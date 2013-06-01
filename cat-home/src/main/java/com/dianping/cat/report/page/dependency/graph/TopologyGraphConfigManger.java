package com.dianping.cat.report.page.dependency.graph;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.tuple.Pair;
import org.unidal.webres.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;
import com.dianping.cat.home.dependency.config.transform.DefaultSaxParser;

public class TopologyGraphConfigManger implements Initializable {

	private TopologyGraphConfig m_config;

	private DecimalFormat m_df = new DecimalFormat("0.0");

	private static final String AVG = "AVG:";

	private static final String ERROR = "ERROR:";

	private static final String SPIT = "\n";

	private static final String DEFAULT_FILE = "/data/appdatas/cat/topology-config.xml";

	private String m_fileName = DEFAULT_FILE;

	public Pair<Integer, String> buildNodeState(String domain, Index index) {
		String type = index.getName();
		String realType = formatType(type);
		DomainConfig config = queryNodeConfig(realType, domain);
		if (config != null) {
			double avg = index.getAvg();
			long error = index.getErrorCount();
			int errorCode = TopologyGraphItemBuilder.OK;
			StringBuilder sb = new StringBuilder();

			if (avg >= config.getErrorResponseTime()) {
				errorCode = TopologyGraphItemBuilder.ERROR;
				sb.append(AVG + m_df.format(avg)).append(SPIT);
			} else if (avg >= config.getWarningResponseTime()) {
				errorCode = TopologyGraphItemBuilder.WARN;
				sb.append(AVG + m_df.format(avg)).append(SPIT);
			}
			if (error >= config.getErrorThreshold()) {
				errorCode = TopologyGraphItemBuilder.ERROR;
				sb.append(ERROR + error).append(SPIT);
			} else if (error >= config.getWarningThreshold()) {
				errorCode = TopologyGraphItemBuilder.WARN;
				sb.append(ERROR + error).append(SPIT);
			}
			if (errorCode != TopologyGraphItemBuilder.OK) {
				Pair<Integer, String> result = new Pair<Integer, String>();

				result.setKey(errorCode);
				result.setValue(sb.toString());
				return result;
			}
		}
		return null;
	}

	private String formatType(String type) {
		String realType = type;
		if (type.startsWith("Cache.")) {
			realType = "Cache";
		} else if ("PigeonCall".equals(type) || "Call".equals(type)) {
			realType = "PigeonCall";
		} else if ("PigeonService".equals(type) || "Service".equals(type)) {
			realType = "PigeonService";
		}
		return realType;
	}

	public Pair<Integer, String> buildEdgeState(String domain, Dependency dependency) {
		String type = dependency.getType();
		String from = domain;
		String to = dependency.getTarget();
		EdgeConfig config = queryEdgeConfig(type, from, to);

		if (config != null) {
			double avg = dependency.getAvg();
			long error = dependency.getErrorCount();
			int errorCode = TopologyGraphItemBuilder.OK;
			StringBuilder sb = new StringBuilder();

			if (avg >= config.getErrorResponseTime()) {
				errorCode = TopologyGraphItemBuilder.ERROR;
				sb.append(AVG + m_df.format(avg)).append(SPIT);
			} else if (avg >= config.getWarningResponseTime()) {
				errorCode = TopologyGraphItemBuilder.WARN;
				sb.append(AVG + m_df.format(avg)).append(SPIT);
			}
			if (error >= config.getErrorThreshold()) {
				errorCode = TopologyGraphItemBuilder.ERROR;
				sb.append(ERROR + error).append(SPIT);
			} else if (error >= config.getWarningThreshold()) {
				errorCode = TopologyGraphItemBuilder.WARN;
				sb.append(ERROR + error).append(SPIT);
			}
			if (errorCode != TopologyGraphItemBuilder.OK) {
				Pair<Integer, String> result = new Pair<Integer, String>();

				result.setKey(errorCode);
				result.setValue(sb.toString());
				return result;
			}
		}
		return null;

	}

	private DomainConfig queryNodeConfig(String type, String domain) {
		NodeConfig typesConfig = m_config.findNodeConfig(type);

		if (typesConfig != null) {
			DomainConfig config = typesConfig.findDomainConfig(domain);
			if (config == null) {
				config = new DomainConfig();

				config.setId(domain);
				config.setErrorResponseTime(typesConfig.getDefaultErrorResponseTime());
				config.setErrorThreshold(typesConfig.getDefaultErrorThreshold());
				config.setWarningResponseTime(typesConfig.getDefaultWarningResponseTime());
				config.setWarningThreshold(typesConfig.getDefaultWarningThreshold());
			}
			return config;
		}
		return null;
	}

	private EdgeConfig queryEdgeConfig(String type, String from, String to) {
		EdgeConfig edgeConfig = m_config.findEdgeConfig(type + ":" + from + ":" + to);

		if (edgeConfig == null) {
			DomainConfig domainConfig = null;
			if ("PigeonCall".equalsIgnoreCase(type)) {
				domainConfig = queryNodeConfig("PigeonService", to);
			} else if ("Database".equalsIgnoreCase(type)) {
				domainConfig = queryNodeConfig("SQL", to);
			} else if ("PigeonServer".equalsIgnoreCase(type)) {
				domainConfig = queryNodeConfig("PigeonService", from);
			}
			if (domainConfig != null) {
				edgeConfig = convertNodeConfig(domainConfig);
			}
		}

		return edgeConfig;
	}

	private EdgeConfig convertNodeConfig(DomainConfig config) {
		EdgeConfig edgeConfig = new EdgeConfig();

		edgeConfig.setWarningResponseTime(config.getWarningResponseTime());
		edgeConfig.setErrorResponseTime(config.getErrorResponseTime());
		edgeConfig.setWarningThreshold(config.getWarningThreshold());
		edgeConfig.setErrorThreshold(config.getErrorThreshold());
		return edgeConfig;
	}

	private boolean flushConfig() {
		String data = m_config.toString();
		try {
			Files.forIO().writeTo(new File(m_fileName), data);
		} catch (IOException e) {
			Cat.logError(e);
			return false;
		}
		return true;
	}

	private String readConfig() throws IOException {
		return Files.forIO().readFrom(new File(m_fileName), "utf-8");
	}

	public boolean insertDomainConfig(String type, DomainConfig config) {
		m_config.findOrCreateNodeConfig(type).addDomainConfig(config);
		return flushConfig();
	}

	public boolean insertEdgeConfig(EdgeConfig config) {
		config.setKey(config.getType() + ":" + config.getFrom() + ":" + config.getTo());
		m_config.addEdgeConfig(config);
		return flushConfig();
	}

	public boolean deleteDomainConfig(String type, String domain) {
		NodeConfig types = m_config.getNodeConfigs().get(type);
		types.removeDomainConfig(domain);
		return flushConfig();
	}

	public boolean deleteEdgeConfig(String type, String from, String to) {
		String key = type + ':' + from + ':' + to;
		m_config.removeEdgeConfig(key);
		return flushConfig();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			String content = readConfig();

			m_config = DefaultSaxParser.parse(content);
		} catch (Exception e) {
			System.err.println(e);
			Cat.logError(e);
		}
		if (m_config == null) {
			m_config = new TopologyGraphConfig();
		}
	}

	public void setFileName(String file) {
		m_fileName = file;
	}

	public TopologyGraphConfig getConfig() {
		return m_config;
	}

}
