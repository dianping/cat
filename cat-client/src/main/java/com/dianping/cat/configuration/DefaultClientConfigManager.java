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

import java.io.File;
import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.site.helper.JsonBuilder;
import com.site.helper.Splitters;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.client.entity.ClientConfig;
import com.dianping.cat.configuration.client.entity.Domain;
import com.dianping.cat.configuration.client.entity.Server;
import com.dianping.cat.configuration.client.transform.DefaultSaxParser;
import com.dianping.cat.message.spi.MessageTree;

@Named(type = ClientConfigManager.class)
public class DefaultClientConfigManager implements LogEnabled, ClientConfigManager, Initializable {

	private static final String PROPERTIES_FILE = "/META-INF/app.properties";

	private ClientConfig m_config;

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

	@Override
	public Domain getDomain() {
		Domain domain = null;

		if (m_config != null) {
			Map<String, Domain> domains = m_config.getDomains();

			domain = domains.isEmpty() ? null : domains.values().iterator().next();
		}

		if (domain != null) {
			return domain;
		} else {
			return new Domain("UNKNOWN").setEnabled(false);
		}
	}

	@Override
	public int getMaxMessageLength() {
		if (m_config == null) {
			return 5000;
		} else {
			return getDomain().getMaxMessageSize();
		}
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
		if (m_config == null) {
			return null;
		} else {
			List<Server> servers = m_config.getServers();
			int size = servers.size();
			int index = (int) (size * Math.random());

			if (index >= 0 && index < size) {
				Server server = servers.get(index);

				Integer httpPort = server.getHttpPort();

				if (httpPort == null || httpPort == 0) {
					httpPort = 8080;
				}
				return String.format("http://%s:%d/cat/s/router?domain=%s&ip=%s&op=json", server.getIp().trim(), httpPort,
										getDomain().getId(), NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
			}
		}
		return null;
	}

	@Override
	public List<Server> getServers() {
		if (m_config == null) {
			return Collections.emptyList();
		} else {
			return m_config.getServers();
		}
	}

	@Override
	public int getTaggedTransactionCacheSize() {
		return 1024;
	}

	@Override
	public void initialize() throws InitializationException {
		String xml = Cat.getCatHome() + "client.xml";
		File configFile = new File(xml);

		m_logger.info("client xml path " + xml);
		initialize(configFile);
	}

	@Override
	public void initialize(File configFile) throws InitializationException {
		try {
			ClientConfig globalConfig = null;
			ClientConfig warConfig = null;

			if (configFile != null) {
				if (configFile.exists()) {
					String xml = Files.forIO().readFrom(configFile.getCanonicalFile(), "utf-8");

					globalConfig = DefaultSaxParser.parse(xml);
					m_logger.info(String.format("Global config file(%s) found.", configFile));
				} else {
					m_logger.warn(String.format("Global config file(%s) not found, IGNORED.", configFile));
				}
			}

			// load the client configure from Java class-path
			warConfig = loadConfigFromEnviroment();

			// merge the two configures together to make it effected
			if (globalConfig != null && warConfig != null) {
				globalConfig.accept(new ClientConfigMerger(warConfig));
			}

			if (warConfig != null) {
				warConfig.accept(new ClientConfigValidator());
			}

			m_config = warConfig;
			refreshConfig();
		} catch (Exception e) {
			throw new InitializationException(e.getMessage(), e);
		}
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
		if (m_config == null) {
			return false;
		} else {
			return m_config.isEnabled();
		}
	}

	@Override
	public boolean isDumpLocked() {
		if (m_config == null) {
			return false;
		} else {
			return m_config.isDumpLocked();
		}
	}

	private ClientConfig loadConfigFromEnviroment() {
		String appName = loadProjectName();

		if (appName != null) {
			ClientConfig config = new ClientConfig();

			config.addDomain(new Domain(appName));
			return config;
		}
		return null;
	}

	private String loadProjectName() {
		String appName = null;
		InputStream in = null;
		try {
			in = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE);

			if (in == null) {
				in = Cat.class.getResourceAsStream(PROPERTIES_FILE);
			}
			if (in != null) {
				Properties prop = new Properties();

				prop.load(in);

				appName = prop.getProperty("app.name");
				if (appName != null) {
					m_logger.info(String.format("Find domain name %s from app.properties.", appName));
				} else {
					m_logger.info(String.format("Can't find app.name from app.properties."));
					return null;
				}
			} else {
				m_logger.info(String.format("Can't find app.properties in %s", PROPERTIES_FILE));
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

}
