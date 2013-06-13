package com.dianping.cat.report.page.dependency.graph;

import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;
import org.unidal.webres.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.core.config.Config;
import com.dianping.cat.consumer.core.config.ConfigDao;
import com.dianping.cat.consumer.core.config.ConfigEntity;
import com.dianping.cat.consumer.dependency.model.entity.Dependency;
import com.dianping.cat.consumer.dependency.model.entity.Index;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.helper.MapUtils;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dependency.config.entity.Domain;
import com.dianping.cat.home.dependency.config.entity.DomainConfig;
import com.dianping.cat.home.dependency.config.entity.EdgeConfig;
import com.dianping.cat.home.dependency.config.entity.NodeConfig;
import com.dianping.cat.home.dependency.config.entity.ProductLine;
import com.dianping.cat.home.dependency.config.entity.TopologyGraphConfig;
import com.dianping.cat.home.dependency.config.transform.DefaultSaxParser;

public class TopologyGraphConfigManager implements Initializable, LogEnabled {
	@Inject
	private ConfigDao m_configDao;

	private TopologyGraphConfig m_config;

	private Map<String, String> m_domainToProductLine = new HashMap<String, String>();

	private DecimalFormat m_df = new DecimalFormat("0.0");

	private static final String AVG_STR = CatString.RESPONSE_TIME;

	private static final String ERROR_STR = CatString.EXCEPTION_COUNT;

	private static final String TOTAL_STR = CatString.TOTAL_COUNT;

	private static final String MILLISECOND = "(ms)";

	private static final int OK = GraphConstrant.OK;

	private static final int WARN = GraphConstrant.WARN;

	private static final int ERROR = GraphConstrant.ERROR;

	private static final String CONFIG_NAME = "topologyConfig";

	private int m_configId;

	private String m_fileName;

	private long m_modifyTime;

	private Set<String> m_pigeonCalls = new HashSet<String>(Arrays.asList("Call", "PigeonCall", "PigeonClient"));

	private Set<String> m_pigeonServices = new HashSet<String>(Arrays.asList("Service", "PigeonService", "PigeonServer"));

	private Logger m_logger;

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
		String type = formatType(dependency.getType());
		String from = domain;
		String to = dependency.getTarget();
		EdgeConfig config = queryEdgeConfig(type, from, to);
		long error = dependency.getErrorCount();
		StringBuilder sb = new StringBuilder();
		int errorCode = OK;

		if (config != null) {
			double avg = dependency.getAvg();
			sb.append(buildDes(type, TOTAL_STR, String.valueOf(dependency.getTotalCount())));

			if (avg >= config.getErrorResponseTime()) {
				errorCode = ERROR;
				sb.append(buildErrorDes(type, AVG_STR, m_df.format(avg), MILLISECOND));
			} else if (avg >= config.getWarningResponseTime()) {
				errorCode = WARN;
				sb.append(buildErrorDes(type, AVG_STR, m_df.format(avg), MILLISECOND));
			} else {
				sb.append(buildDes(type, AVG_STR, m_df.format(avg), MILLISECOND));
			}
			if (error >= config.getErrorThreshold()) {
				errorCode = ERROR;
				sb.append(buildErrorDes(type, ERROR_STR, String.valueOf(error)));
			} else if (error >= config.getWarningThreshold()) {
				errorCode = WARN;
				sb.append(buildErrorDes(type, ERROR_STR, String.valueOf(error)));
			} else if (error > 0) {
				sb.append(buildDes(type, ERROR_STR, String.valueOf(error)));
			}
		}
		Pair<Integer, String> result = new Pair<Integer, String>();

		result.setKey(errorCode);
		result.setValue(sb.toString());
		return result;
	}

	private String buildErrorDes(String... args) {
		StringBuilder sb = new StringBuilder("<span style='color:red'>");
		int len = args.length;

		for (int i = 0; i < len - 1; i++) {
			sb.append(args[i]).append(GraphConstrant.DELIMITER);
		}
		sb.append(args[len - 1]).append("</span>").append(GraphConstrant.ENTER);
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
			sb.append(buildDes(type, TOTAL_STR, String.valueOf(index.getTotalCount())));

			if (avg >= config.getErrorResponseTime()) {
				errorCode = ERROR;
				sb.append(buildErrorDes(type, AVG_STR, m_df.format(avg), MILLISECOND));
			} else if (avg >= config.getWarningResponseTime()) {
				errorCode = WARN;
				sb.append(buildErrorDes(type, AVG_STR, m_df.format(avg), MILLISECOND));
			} else {
				if (!type.equals("Exception")) {
					sb.append(buildDes(type, AVG_STR, m_df.format(avg), MILLISECOND));
				}
			}
			if (error >= config.getErrorThreshold()) {
				errorCode = ERROR;
				sb.append(buildErrorDes(type, ERROR_STR, String.valueOf(error)));
			} else if (error >= config.getWarningThreshold()) {
				errorCode = WARN;
				sb.append(buildErrorDes(type, ERROR_STR, String.valueOf(error)));
			} else if (error > 0) {
				sb.append(buildDes(type, ERROR_STR, String.valueOf(error)));
			}
		}
		Pair<Integer, String> result = new Pair<Integer, String>();

		result.setKey(errorCode);
		result.setValue(sb.toString());
		return result;
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
		return storeConfig();
	}

	public boolean deleteEdgeConfig(String type, String from, String to) {
		String key = type + ':' + from + ':' + to;
		m_config.removeEdgeConfig(key);
		return storeConfig();
	}

	public boolean deleteProductLine(String line) {
		m_config.removeProductLine(line);
		return storeConfig();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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

				m_config = DefaultSaxParser.parse(content);
				m_configId = config.getId();
				m_modifyTime = config.getModifyDate().getTime();
			} catch (DalNotFoundException e) {
				try {
					String content = Files.forIO().readFrom(
					      this.getClass().getResourceAsStream("/config/default-topology-config.xml"), "utf-8");
					Config config = m_configDao.createLocal();

					config.setName(CONFIG_NAME);
					config.setContent(content);
					m_configDao.insert(config);

					m_config = DefaultSaxParser.parse(content);
					m_configId = config.getId();
					m_modifyTime = config.getModifyDate().getTime();
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

		Threads.forGroup("Cat").start(new Reload());
	}

	public boolean insertDomainConfig(String type, DomainConfig config) {
		m_config.findOrCreateNodeConfig(type).addDomainConfig(config);
		return storeConfig();
	}

	public boolean insertDomainDefaultConfig(String type, DomainConfig config) {
		NodeConfig node = m_config.findOrCreateNodeConfig(type);

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

	public boolean insertProductLine(ProductLine line, String[] domains) {
		m_config.removeProductLine(line.getId());
		m_config.addProductLine(line);

		for (String domain : domains) {
			line.addDomain(new Domain(domain));
		}
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
				config.setErrorResponseTime(typesConfig.getDefaultErrorResponseTime());
				config.setErrorThreshold(typesConfig.getDefaultErrorThreshold());
				config.setWarningResponseTime(typesConfig.getDefaultWarningResponseTime());
				config.setWarningThreshold(typesConfig.getDefaultWarningThreshold());
			}
			return config;
		}
		return null;
	}

	public String queryProductLineByDomain(String domain) {
		return m_domainToProductLine.get(domain);
	}

	public List<String> queryProductLineDomains(String productLine) {
		List<String> domains = new ArrayList<String>();
		ProductLine line = m_config.findProductLine(productLine);

		if (line != null) {
			for (Domain domain : line.getDomains().values()) {
				domains.add(domain.getId());
			}
		}
		return domains;
	}

	public Map<String, ProductLine> queryProductLines() {
		Map<String, ProductLine> productLines = new TreeMap<String, ProductLine>();

		for (ProductLine line : m_config.getProductLines().values()) {
			productLines.put(line.getId(), line);
		}
		return MapUtils.sortMap(productLines, new Comparator<Map.Entry<String, ProductLine>>() {

			@Override
			public int compare(Entry<String, ProductLine> o1, Entry<String, ProductLine> o2) {
				return (int) (o2.getValue().getOrder() * 100 - o1.getValue().getOrder() * 100);
			}
		});

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

		Map<String, ProductLine> productLines = m_config.getProductLines();
		Map<String, String> domainToProductLine = new HashMap<String, String>();

		for (ProductLine product : productLines.values()) {
			for (Domain domain : product.getDomains().values()) {
				domainToProductLine.put(domain.getId(), product.getId());
			}
		}
		m_domainToProductLine = domainToProductLine;
		return true;
	}

	public class Reload implements Task {

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void run() {
			boolean active = true;
			while (active) {
				try {
					Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
					long modifyTime = config.getModifyDate().getTime();

					if (modifyTime > m_modifyTime) {
						String content = config.getContent();

						synchronized (m_config) {
							m_config = DefaultSaxParser.parse(content);
						}

						m_modifyTime = modifyTime;
						m_logger.info("Topology config refresh done!");
					}
				} catch (Exception e) {
					Cat.logError(e);
				}

				try {
					Thread.sleep(TimeUtil.ONE_MINUTE);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

}
