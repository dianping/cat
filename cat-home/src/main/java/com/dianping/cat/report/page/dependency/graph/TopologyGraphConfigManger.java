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
import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;
import com.dianping.cat.home.dependency.config.transform.DefaultSaxParser;

public class TopologyGraphConfigManger implements Initializable {

	private TopologyGraphConfig m_config;

	private DecimalFormat m_df = new DecimalFormat("0.0");

	private static final String AVG_STR = CatString.RESPONSE_TIME;

	private static final String ERROR_STR = CatString.ERROR_COUNT;

	private static final String MILLISECOND = "(ms)";

	private static final String DEFAULT_FILE = "/data/appdatas/cat/topology-config.xml";

	private static final int OK = GraphConstrant.OK;

	private static final int WARN = GraphConstrant.WARN;

	private static final int ERROR = GraphConstrant.ERROR;

	private String m_fileName = DEFAULT_FILE;

	private String buildDes(String... args) {
		StringBuilder sb = new StringBuilder();
		int len = args.length;

		for (int i = 0; i < len - 1; i++) {
			sb.append(args[i]).append(GraphConstrant.DELIMITER);
		}
		sb.append(args[len - 1]).append(GraphConstrant.ENTER);

		return sb.toString();
	}

	public Pair<Integer, String> buildEdgeState(String domain, Dependency dependency) {
		String type = dependency.getType();
		String from = domain;
		String to = dependency.getTarget();
		EdgeConfig config = queryEdgeConfig(type, from, to);

		if (config != null) {
			double avg = dependency.getAvg();
			long error = dependency.getErrorCount();
			int errorCode = OK;
			StringBuilder sb = new StringBuilder();

			if (avg >= config.getErrorResponseTime()) {
				errorCode = ERROR;
				sb.append(buildDes(AVG_STR, m_df.format(avg), MILLISECOND));
			} else if (avg >= config.getWarningResponseTime()) {
				errorCode = WARN;
				sb.append(buildDes(AVG_STR, m_df.format(avg), MILLISECOND));
			}
			if (error >= config.getErrorThreshold()) {
				errorCode = ERROR;
				sb.append(buildDes(ERROR_STR, String.valueOf(error)));
			} else if (error >= config.getWarningThreshold()) {
				errorCode = WARN;
				sb.append(buildDes(ERROR_STR, String.valueOf(error)));
			}
			if (errorCode != OK) {
				Pair<Integer, String> result = new Pair<Integer, String>();

				result.setKey(errorCode);
				result.setValue(sb.toString());
				return result;
			}
		}
		return null;

	}

	public Pair<Integer, String> buildNodeState(String domain, Index index) {
		String type = index.getName();
		String realType = formatType(type);
		DomainConfig config = queryNodeConfig(realType, domain);
		if (config != null) {
			double avg = index.getAvg();
			long error = index.getErrorCount();
			int errorCode = OK;
			StringBuilder sb = new StringBuilder();

			if (avg >= config.getErrorResponseTime()) {
				errorCode = ERROR;
				sb.append(buildDes(type, AVG_STR, m_df.format(avg), MILLISECOND));
			} else if (avg >= config.getWarningResponseTime()) {
				errorCode = WARN;
				sb.append(buildDes(type, AVG_STR, m_df.format(avg), MILLISECOND));
			}
			if (error >= config.getErrorThreshold()) {
				errorCode = ERROR;
				sb.append(buildDes(type, ERROR_STR, String.valueOf(error)));
			} else if (error >= config.getWarningThreshold()) {
				errorCode = WARN;
				sb.append(buildDes(type, ERROR_STR, String.valueOf(error)));
			}
			if (errorCode != OK) {
				Pair<Integer, String> result = new Pair<Integer, String>();

				result.setKey(errorCode);
				result.setValue(sb.toString());
				return result;
			}
		}
		return null;
	}

	private EdgeConfig convertNodeConfig(DomainConfig config) {
		EdgeConfig edgeConfig = new EdgeConfig();

		edgeConfig.setWarningResponseTime(config.getWarningResponseTime());
		edgeConfig.setErrorResponseTime(config.getErrorResponseTime());
		edgeConfig.setWarningThreshold(config.getWarningThreshold());
		edgeConfig.setErrorThreshold(config.getErrorThreshold());
		return edgeConfig;
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

	public TopologyGraphConfig getConfig() {
		return m_config;
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

	public boolean insertDomainConfig(String type, DomainConfig config) {
		m_config.findOrCreateNodeConfig(type).addDomainConfig(config);
		return flushConfig();
	}

	public boolean insertEdgeConfig(EdgeConfig config) {
		config.setKey(config.getType() + ":" + config.getFrom() + ":" + config.getTo());
		m_config.addEdgeConfig(config);
		return flushConfig();
	}

	private EdgeConfig queryEdgeConfig(String type, String from, String to) {
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

	private String readConfig() throws IOException {
		return Files.forIO().readFrom(new File(m_fileName), "utf-8");
	}

	public void setFileName(String file) {
		m_fileName = file;
	}

}
