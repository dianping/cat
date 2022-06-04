/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.configuration;

import static com.dianping.cat.CatClientConstants.APP_PROPERTIES;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.Cat;
import com.dianping.cat.CatClientConstants;
import com.dianping.cat.component.ComponentContext;
import com.dianping.cat.component.Logger;
import com.dianping.cat.component.lifecycle.Initializable;
import com.dianping.cat.component.lifecycle.LogEnabled;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.BaseVisitor;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.util.Files;
import com.dianping.cat.util.Splitters;
import com.dianping.cat.util.Urls;

// Component
public class DefaultClientConfigManager implements ClientConfigManager, Initializable, LogEnabled {
	// Inject
	private ApplicationProperties m_properties;

	private ClientConfig m_config = new ClientConfig();

	private ServerConfig m_serverConfig = new ServerConfig();

	private AtomicBoolean m_initialized = new AtomicBoolean();

	private AtomicTreeParser m_atomicTreeParser = new AtomicTreeParser();

	private Map<String, List<Integer>> m_longConfigs = new LinkedHashMap<String, List<Integer>>();

	private Logger m_logger;

	@Override
	public void configure(ClientConfig source) {
		source.accept(new ConfigExtractor(m_config));
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private ClientConfig getConfig() {
		if (!m_initialized.get()) {
			m_config.accept(new ClientXmlLoader());
			m_config.accept(new AppPropertyLoader());
			m_config.accept(new ConfigValidator());

			m_initialized.set(true);
		}

		return m_config;
	}

	@Override
	public Domain getDomain() {
		ClientConfig config = getConfig();

		for (Domain domain : config.getDomains().values()) {
			return domain;
		}

		// shouldn't reach here
		return new Domain("Unknown").setEnabled(true);
	}

	@Override
	public int getLongThresholdByDuration(String key, int duration) {
		List<Integer> values = m_longConfigs.get(key);

		if (values != null) {
			for (int i = values.size() - 1; i >= 0; i--) {
				int userThreshold = values.get(i);

				if (duration >= userThreshold) {
					return userThreshold;
				}
			}
		}

		return -1;
	}

	@Override
	public String getRouters() {
		return m_serverConfig.getProperty("routers", null);
	}

	public double getSampleRatio() {
		double sample = m_serverConfig.getDoubleProperty("sample", 1.0);

		if (sample < 0) {
			sample = 0;
		}

		return sample;
	}

	@Override
	public int getSenderQueueSize() {
		return m_properties.getIntProperty("cat.queue.length", 5000);
	}

	@Override
	public List<Server> getServers() {
		return getConfig().getServers();
	}

	@Override
	public int getTaggedTransactionCacheSize() {
		return 1024;
	}

	@Override
	public int getTreeLengthLimit() {
		return m_properties.getIntProperty("cat.tree.max.length", 2000);
	}

	@Override
	public void initialize(ComponentContext ctx) {
		m_properties = ctx.lookup(ApplicationProperties.class);
	}

	@Override
	public boolean isAtomicMessage(MessageTree tree) {
		return m_atomicTreeParser.isAtomicMessage(tree);
	}

	public boolean isBlock() {
		return m_serverConfig.getBooleanProperty("block", false);
	}

	@Override
	public boolean isCatEnabled() {
		return getConfig().isEnabled();
	}

	@Override
	public boolean isDumpLocked() {
		return getConfig().isDumpLocked();
	}

	public void refreshConfig() {
		m_serverConfig.refresh();

		String startTypes = m_serverConfig.getProperty("startTransactionTypes", "");
		String matchTypes = m_serverConfig.getProperty("matchTransactionTypes", "");

		m_atomicTreeParser.init(startTypes, matchTypes);

		for (ProblemLongType longType : ProblemLongType.values()) {
			final String name = longType.getName();
			String propertyName = name + "s";
			String values = m_serverConfig.getProperty(propertyName, null);

			if (values != null) {
				List<String> valueStrs = Splitters.by(',').trim().split(values);
				List<Integer> thresholds = new LinkedList<Integer>();

				for (String valueStr : valueStrs) {
					try {
						thresholds.add(Integer.parseInt(valueStr));
					} catch (Exception e) {
						// ignore
					}
				}

				if (!thresholds.isEmpty()) {
					m_longConfigs.put(name, thresholds);
				}
			}
		}
	}

	// if no domain specified, then try to get it from /META-INF/app.properties
	private class AppPropertyLoader extends BaseVisitor {
		@Override
		public void visitConfig(ClientConfig config) {
			if (config.getDomains().isEmpty()) {
				String name = m_properties.getProperty("app.name", null);

				if (name == null) {
					m_logger.info(String.format("No property(app.name) defined in resource(%s)!", APP_PROPERTIES));
				} else {
					Domain d = new Domain(name).setEnabled(true);

					config.addDomain(d);
				}
			}
		}
	}

	// try to figure out the ClientConfig by ${CAT_HOME}/client.xml
	// if anything is missing
	private class ClientXmlLoader extends BaseVisitor {
		@Override
		public void visitConfig(ClientConfig config) {
			if (config.getDomains().isEmpty() || config.getServers().isEmpty()) {
				File configFile = new File(Cat.getCatHome(), CatClientConstants.CLIENT_XML);

				if (configFile.exists()) {
					try {
						ClientConfig c = DefaultSaxParser.parse(new FileInputStream(configFile));

						// fill m_config with config from client.xml
						super.visitConfig(c);
					} catch (Exception e) {
						m_logger.error(e.getMessage(), e);
					}
				}
			}
		}

		@Override
		public void visitDomain(Domain domain) {
			String id = domain.getId().trim();
			Domain d = m_config.findDomain(id);

			if (d == null) {
				d = new Domain().setId(id);
				m_config.addDomain(d);
			}

			d.mergeAttributes(domain);
		}

		@Override
		public void visitServer(Server server) {
			String ip = server.getIp().trim();
			Server s = m_config.findServer(ip);

			if (s == null) {
				s = new Server().setIp(ip);
				m_config.addServer(s);
			}

			s.mergeAttributes(server);
		}
	}

	// fill the ClientConfig with given config
	private static class ConfigExtractor extends BaseVisitor {
		private ClientConfig m_config;

		public ConfigExtractor(ClientConfig config) {
			m_config = config;
		}

		@Override
		public void visitDomain(Domain domain) {
			Domain d = m_config.findDomain(domain.getId());

			if (d == null) {
				d = new Domain().setId(domain.getId());
				m_config.addDomain(d);
			}

			d.mergeAttributes(domain);
		}

		@Override
		public void visitServer(Server server) {
			Server s = m_config.findServer(server.getIp());

			if (s == null) {
				s = new Server().setIp(server.getIp());
				m_config.addServer(s);
			}

			s.mergeAttributes(server);
		}
	}

	// check if the ClientConfig is well prepared
	// DISABLE CAT if anything required is missing
	private static class ConfigValidator extends BaseVisitor {
		@Override
		public void visitConfig(ClientConfig config) {
			if (config.getDomains().isEmpty()) {
				config.addDomain(new Domain("Unknown").setEnabled(true));
			}

			if (config.getServers().isEmpty()) {
				config.setEnabled(false);
			}
		}
	}

	private class ServerConfig {
		private Map<String, String> m_properties = new HashMap<String, String>();

		public void refresh() {
			Map<String, String> properties = new HashMap<String, String>();
			String url = null;

			for (Server server : getConfig().getServers()) {
				try {
					String ip = server.getIp();
					int port = server.getHttpPort();
					String localIp = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
					String domain = getDomain().getId();

					url = String.format("http://%s:%d/cat/s/router?domain=%s&ip=%s&op=json", ip, port, domain, localIp);

					InputStream inputstream = Urls.forIO().connectTimeout(1000).readTimeout(1000).openStream(url);
					String content = Files.forIO().readFrom(inputstream, "utf-8");

					parseMap(properties, content.trim());
					break;
				} catch (IOException e) {
					// ignore it
					m_logger.warn(String.format("Error when requesting %s. Reason: %s. IGNORED", url, e));
				}
			}

			if (!properties.isEmpty()) {
				m_properties = properties;
			}
		}

		public boolean getBooleanProperty(String key, boolean defaultValue) {
			String property = getProperty(key, null);

			if (property != null) {
				try {
					return Boolean.valueOf(property);
				} catch (NumberFormatException e) {
					// ignore it
				}
			}

			return defaultValue;
		}

		public double getDoubleProperty(String key, double defaultValue) {
			String property = getProperty(key, null);

			if (property != null) {
				try {
					return Double.parseDouble(property);
				} catch (NumberFormatException e) {
					// ignore it
				}
			}

			return defaultValue;
		}

		public String getProperty(String key, String defaultValue) {
			String property = m_properties.get(key);

			if (property != null) {
				return property;
			}

			return defaultValue;
		}

		private void parseMap(Map<String, String> map, String content) {
			if (content.startsWith("{") && content.endsWith("}")) {
				String keyValuePairs = content.substring(1, content.length() - 1);

				Splitters.by(',', '=').trim().split(keyValuePairs, map);
			}
		}
	}
}
