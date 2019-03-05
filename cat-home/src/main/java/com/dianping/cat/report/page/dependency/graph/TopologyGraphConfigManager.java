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
package com.dianping.cat.report.page.dependency.graph;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;
import com.dianping.cat.home.dependency.config.transform.DefaultSaxParser;

@Named
public class TopologyGraphConfigManager implements Initializable {
	private static final String AVG_STR = Chinese.RESPONSE_TIME;

	private static final String ERROR_STR = Chinese.EXCEPTION_COUNT;

	private static final String TOTAL_STR = Chinese.TOTAL_COUNT;

	private static final String MILLISECOND = "(ms)";

	private static final int OK = GraphConstrant.OK;

	private static final int WARN = GraphConstrant.WARN;

	private static final int ERROR = GraphConstrant.ERROR;

	private static final String CONFIG_NAME = "topologyConfig";

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private TopologyGraphConfig m_config;

	private DecimalFormat m_df = new DecimalFormat("0.0");

	private int m_configId;

	private String m_fileName;

	private Set<String> m_pigeonCalls = new HashSet<String>(Arrays.asList("Call", "PigeonCall", "PigeonClient"));

	private Set<String> m_pigeonServices = new HashSet<String>(Arrays.asList("Service", "PigeonService", "PigeonServer"));

	private String buildDes(String... args) {
		StringBuilder sb = new StringBuilder();
		int len = args.length;

		for (int i = 0; i < len; i++) {
			sb.append(args[i]).append(GraphConstrant.DELIMITER);
		}

		return sb.toString();
	}

	public Pair<Integer, String> buildEdgeState(String domain, Dependency dependency) {
		String type = formatType(dependency.getType());
		String from = domain;
		String to = dependency.getTarget();
		EdgeConfig config = queryEdgeConfig(type, from, to);
		long error = dependency.getErrorCount();
		StringBuilder sb = new StringBuilder();
		int errorCode = OK;

		if (config != null) {
			double avg = dependency.getAvg();
			long totalCount = dependency.getTotalCount();
			int minCount = config.getMinCountThreshold();

			sb.append(buildDes(type, TOTAL_STR, String.valueOf(dependency.getTotalCount()))).append(GraphConstrant.ENTER);

			if (avg >= config.getErrorResponseTime() && totalCount > minCount) {
				errorCode = ERROR;
				sb.append(buildErrorDes(type, AVG_STR, m_df.format(avg), MILLISECOND)).append(GraphConstrant.ENTER);
			} else if (avg >= config.getWarningResponseTime() && totalCount > minCount) {
				errorCode = WARN;
				sb.append(buildWarningDes(type, AVG_STR, m_df.format(avg), MILLISECOND)).append(GraphConstrant.ENTER);
			} else {
				sb.append(buildDes(type, AVG_STR, m_df.format(avg), MILLISECOND)).append(GraphConstrant.ENTER);
			}
			if (error >= config.getErrorThreshold() && totalCount > minCount) {
				errorCode = ERROR;
				sb.append(buildErrorDes(type, ERROR_STR, String.valueOf(error))).append(GraphConstrant.ENTER);
			} else if (error >= config.getWarningThreshold() && totalCount > minCount) {
				errorCode = WARN;
				sb.append(buildWarningDes(type, ERROR_STR, String.valueOf(error))).append(GraphConstrant.ENTER);
			} else if (error > 0) {
				sb.append(buildDes(type, ERROR_STR, String.valueOf(error))).append(GraphConstrant.ENTER);
			}
		}
		Pair<Integer, String> result = new Pair<Integer, String>();

		result.setKey(errorCode);
		result.setValue(sb.toString());
		return result;
	}

	private String buildErrorDes(String... args) {
		StringBuilder sb = new StringBuilder("<span style='color:red'>");
		String content = buildDes(args);

		sb.append(content).append("</span>");
		return sb.toString();
	}

	public Pair<Integer, String> buildNodeState(String domain, Index index) {
		String type = index.getName();
		String realType = formatType(type);
		DomainConfig config = queryNodeConfig(realType, domain);
		int errorCode = OK;
		StringBuilder sb = new StringBuilder();

		if (config != null) {
			double avg = index.getAvg();
			long error = index.getErrorCount();
			long totalCount = index.getTotalCount();
			int minCount = config.getMinCountThreshold();

			sb.append(type).append(GraphConstrant.DELIMITER);

			if (index.getTotalCount() > 0 && !type.equalsIgnoreCase("Exception")) {
				sb.append(buildDes(TOTAL_STR, String.valueOf(index.getTotalCount())));
			}
			if (avg >= config.getErrorResponseTime() && totalCount > minCount) {
				errorCode = ERROR;
				sb.append(buildErrorDes(AVG_STR, m_df.format(avg), MILLISECOND));
			} else if (avg >= config.getWarningResponseTime() && totalCount > minCount) {
				errorCode = WARN;
				sb.append(buildWarningDes(AVG_STR, m_df.format(avg), MILLISECOND));
			} else {
				if (!type.equalsIgnoreCase("Exception")) {
					sb.append(buildDes(AVG_STR, m_df.format(avg), MILLISECOND));
				}
			}
			if (error >= config.getErrorThreshold() && totalCount > minCount) {
				errorCode = ERROR;
				sb.append(buildErrorDes(ERROR_STR, String.valueOf(error)));
			} else if (error >= config.getWarningThreshold() && totalCount > minCount) {
				errorCode = WARN;
				sb.append(buildWarningDes(ERROR_STR, String.valueOf(error)));
			} else if (error > 0) {
				sb.append(buildDes(ERROR_STR, String.valueOf(error)));
			}
			sb.append(GraphConstrant.ENTER);
		}
		Pair<Integer, String> result = new Pair<Integer, String>();

		result.setKey(errorCode);
		result.setValue(sb.toString());
		return result;
	}

	private String buildWarningDes(String... args) {
		StringBuilder sb = new StringBuilder("<span style='color:#bfa22f'>");
		String content = buildDes(args);

		sb.append(content).append("</span>");
		return sb.toString();
	}

	private EdgeConfig convertNodeConfig(DomainConfig config) {
		EdgeConfig edgeConfig = new EdgeConfig();

		edgeConfig.setMinCountThreshold(config.getMinCountThreshold());
		edgeConfig.setWarningResponseTime(config.getWarningResponseTime());
		edgeConfig.setErrorResponseTime(config.getErrorResponseTime());
		edgeConfig.setWarningThreshold(config.getWarningThreshold());
		edgeConfig.setErrorThreshold(config.getErrorThreshold());
		return edgeConfig;
	}

	public boolean deleteDomainConfig(String type, String domain) {
		NodeConfig types = m_config.getNodeConfigs().get(type);
		types.removeDomainConfig(domain);
		return storeConfig();
	}

	public boolean deleteEdgeConfig(String type, String from, String to) {
		String key = type + ':' + from + ':' + to;
		m_config.removeEdgeConfig(key);
		return storeConfig();
	}

	private String formatType(String type) {
		String realType = type;
		if (type.startsWith("Cache.")) {
			realType = "Cache";
		} else if (m_pigeonCalls.contains(type)) {
			realType = "PigeonCall";
		} else if (m_pigeonServices.contains(type)) {
			realType = "PigeonService";
		}
		return realType;
	}

	public synchronized TopologyGraphConfig getConfig() {
		return m_config;
	}

	@Override
	public void initialize() throws InitializationException {
		if (m_fileName != null) {
			try {
				String content = Files.forIO().readFrom(new File(m_fileName), "utf-8");
				m_config = DefaultSaxParser.parse(content);
			} catch (Exception e) {
				Cat.logError(e);
			}
		} else {
			try {
				Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
				String content = config.getContent();

				m_configId = config.getId();
				m_config = DefaultSaxParser.parse(content);
			} catch (DalNotFoundException e) {
				try {
					String content = m_fetcher.getConfigContent(CONFIG_NAME);
					Config config = m_configDao.createLocal();

					config.setName(CONFIG_NAME);
					config.setContent(content);
					m_configDao.insert(config);

					m_configId = config.getId();
					m_config = DefaultSaxParser.parse(content);
				} catch (Exception ex) {
					Cat.logError(ex);
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			if (m_config == null) {
				m_config = new TopologyGraphConfig();
			}
		}
	}

	public boolean insertDomainConfig(String type, DomainConfig config) {
		m_config.findOrCreateNodeConfig(type).addDomainConfig(config);
		return storeConfig();
	}

	public boolean insertDomainDefaultConfig(String type, DomainConfig config) {
		NodeConfig node = m_config.findOrCreateNodeConfig(type);

		node.setDefaultMinCountThreshold(config.getMinCountThreshold());
		node.setDefaultErrorResponseTime(config.getErrorResponseTime());
		node.setDefaultErrorThreshold(config.getErrorThreshold());
		node.setDefaultWarningResponseTime(config.getWarningResponseTime());
		node.setDefaultWarningThreshold(config.getWarningThreshold());
		return storeConfig();
	}

	public boolean insertEdgeConfig(EdgeConfig config) {
		config.setKey(config.getType() + ":" + config.getFrom() + ":" + config.getTo());
		m_config.addEdgeConfig(config);
		return storeConfig();
	}

	public EdgeConfig queryEdgeConfig(String type, String from, String to) {
		EdgeConfig edgeConfig = m_config.findEdgeConfig(type + ":" + from + ":" + to);

		if (edgeConfig == null) {
			DomainConfig domainConfig = null;
			if ("PigeonCall".equalsIgnoreCase(type)) {
				domainConfig = queryNodeConfig("PigeonService", to);
			} else if ("PigeonServer".equalsIgnoreCase(type)) {
				domainConfig = queryNodeConfig("PigeonService", from);
			} else {
				domainConfig = queryNodeConfig(type, to);
			}
			if (domainConfig != null) {
				edgeConfig = convertNodeConfig(domainConfig);
			}
		}
		return edgeConfig;
	}

	public DomainConfig queryNodeConfig(String type, String domain) {
		NodeConfig typesConfig = m_config.findNodeConfig(type);

		if (typesConfig != null) {
			DomainConfig config = typesConfig.findDomainConfig(domain);
			if (config == null) {
				config = new DomainConfig();

				config.setId(domain);
				config.setMinCountThreshold(typesConfig.getDefaultMinCountThreshold());
				config.setErrorResponseTime(typesConfig.getDefaultErrorResponseTime());
				config.setErrorThreshold(typesConfig.getDefaultErrorThreshold());
				config.setWarningResponseTime(typesConfig.getDefaultWarningResponseTime());
				config.setWarningThreshold(typesConfig.getDefaultWarningThreshold());
			}
			return config;
		}
		return null;
	}

	public void setFileName(String file) {
		m_fileName = file;
	}

	private boolean storeConfig() {
		if (m_fileName != null) {
			try {
				Files.forIO().writeTo(new File(m_fileName), m_config.toString());
			} catch (IOException e) {
				Cat.logError(e);
				return false;
			}
		} else {
			try {
				Config config = m_configDao.createLocal();
				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_config.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}

		return true;
	}

}
