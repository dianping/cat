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

import static com.dianping.cat.CatClientConstants.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.cat.Cat;
import com.dianping.cat.CatClientConstants;
import com.dianping.cat.component.Logger;
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
import com.dianping.cat.util.json.JsonBuilder;

// Component
public class DefaultClientConfigManager implements LogEnabled, ClientConfigManager {
	private ClientConfig m_config = new ClientConfig();

	private AtomicBoolean m_initialized = new AtomicBoolean();

	private volatile double m_sampleRate = 1d;

	private volatile boolean m_block = false;

	private String m_routers;

	private JsonBuilder m_jsonBuilder = new JsonBuilder();

	private AtomicTreeParser m_atomicTreeParser = new AtomicTreeParser();

	private Map<String, List<Integer>> m_longConfigs = new LinkedHashMap<String, List<Integer>>();

	private Logger m_logger;

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
		if (m_routers == null) {
			refreshConfig();
		}

		return m_routers;
	}

	public double getSampleRatio() {
		return m_sampleRate;
	}

	private String getServerConfigUrl() {
		List<Server> servers = getConfig().getServers();
		int size = servers.size();
		int index = (int) (size * Math.random());

		if (index >= 0 && index < size) {
			Server server = servers.get(index);
			String ip = server.getIp().trim();
			int port = server.getHttpPort();
			String localIp = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			String domain = getDomain().getId();

			return String.format("http://%s:%d/cat/s/router?domain=%s&ip=%s&op=json", ip, port, domain, localIp);
		} else {
			return null;
		}
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
	public void initialize(ClientConfig source) {
		source.accept(new ConfigExtractor(m_config));
	}

	@Override
	public boolean isAtomicMessage(MessageTree tree) {
		return m_atomicTreeParser.isAtomicMessage(tree);
	}

	public boolean isBlock() {
		return m_block;
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
		String url = getServerConfigUrl();

		try {
			InputStream inputstream = Urls.forIO().readTimeout(2000).connectTimeout(1000).openStream(url);
			String content = Files.forIO().readFrom(inputstream, "utf-8");
			KVConfig routerConfig = (KVConfig) m_jsonBuilder.parse(content.trim(), KVConfig.class);

			m_routers = routerConfig.getValue("routers");
			m_block = Boolean.valueOf(routerConfig.getValue("block").trim());

			m_sampleRate = Double.valueOf(routerConfig.getValue("sample").trim());
			if (m_sampleRate <= 0) {
				m_sampleRate = 0;
			}

			String startTypes = routerConfig.getValue("startTransactionTypes");
			String matchTypes = routerConfig.getValue("matchTransactionTypes");

			m_atomicTreeParser.init(startTypes, matchTypes);

			for (ProblemLongType longType : ProblemLongType.values()) {
				final String name = longType.getName();
				String propertyName = name + "s";
				String values = routerConfig.getValue(propertyName);

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
		} catch (Exception e) {
			m_logger.warn("error when connect cat server config url " + url);
		}
	}

	// if no domain specified, then try to get it from /META-INF/app.properties
	private class AppPropertyLoader extends BaseVisitor {
		private String getAppNameFromProperties() {
			String appName = "Unknown";
			InputStream in = null;

			try {
				in = Thread.currentThread().getContextClassLoader().getResourceAsStream(APP_PROPERTIES);

				if (in == null) {
					in = Cat.class.getResourceAsStream(APP_PROPERTIES);
				}

				if (in != null) {
					Properties prop = new Properties();

					prop.load(in);
					appName = prop.getProperty("app.name");

					if (appName == null) {
						m_logger.info(String.format("No property(app.name) defined in resource(%s)!", APP_PROPERTIES));
					}
				} else {
					m_logger.info(String.format("No resource(%s) found!", APP_PROPERTIES));
				}
			} catch (Exception e) {
				m_logger.error(e.getMessage(), e);
			} finally {
				if (in != null) {
					try {
						in.close();
					} catch (Exception e) {
					}
				}
			}

			return appName;
		}

		@Override
		public void visitConfig(ClientConfig config) {
			if (config.getDomains().isEmpty()) {
				String name = getAppNameFromProperties();
				Domain d = new Domain(name).setEnabled(true);

				config.addDomain(d);
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
				config.setEnabled(false);
			}

			if (config.getServers().isEmpty()) {
				config.setEnabled(false);
			}
		}
	}
}
