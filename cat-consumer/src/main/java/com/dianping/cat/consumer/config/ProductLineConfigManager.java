package com.dianping.cat.consumer.config;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.extension.Initializable;
import org.unidal.lookup.extension.InitializationException;
import org.unidal.lookup.logging.LogEnabled;
import org.unidal.lookup.logging.Logger;
import org.unidal.lookup.util.StringUtils;
import org.unidal.tuple.Pair;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.consumer.company.model.entity.Company;
import com.dianping.cat.consumer.company.model.entity.Domain;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.company.model.transform.DefaultSaxParser;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;

public class ProductLineConfigManager implements Initializable, LogEnabled {

	@Inject
	protected ConfigDao m_configDao;

	@Inject
	protected ContentFetcher m_fetcher;

	private Logger m_logger;

	private volatile Map<String, String> m_metricProductLines = new HashMap<String, String>();

	public static final String CONFIG_NAME = "productLineConfig";

	private String buildDomainInfo(ProductLineConfig productLineConfig, ProductLine productline, String[] domainIds) {
		Map<String, String> domains = new HashMap<String, String>();
		String id = productline.getId();
		String duplicateDomains = "";

		if (productLineConfig.needCheckDuplicate()) {
			for (ProductLineConfig config : ProductLineConfig.values()) {
				if (config.needCheckDuplicate()) {
					for (ProductLine product : config.getCompany().getProductLines().values()) {
						String productId = product.getId();

						if (productId != null && !productId.equals(id)) {
							for (Domain domain : product.getDomains().values()) {
								domains.put(domain.getId(), productId);
							}
						}
					}
				}
			}
			for (String domain : domainIds) {
				if (domains.containsKey(domain)) {
					duplicateDomains += domain + "[" + domains.get(domain) + "], ";
				} else {
					productline.addDomain(new Domain(domain));
				}
			}
		} else {
			for (String domain : domainIds) {
				productline.addDomain(new Domain(domain));
			}
		}
		return duplicateDomains;
	}

	private Map<String, String> buildMetricProductLines() {
		Map<String, String> domainToProductLines = new HashMap<String, String>();

		for (ProductLine product : ProductLineConfig.METRIC.getCompany().getProductLines().values()) {
			for (Domain domain : product.getDomains().values()) {
				domainToProductLines.put(domain.getId(), product.getId());
			}
		}
		return domainToProductLines;
	}

	public boolean deleteProductLine(String line, String title) {
		ProductLineConfig productLineConfig = queryProductLineByTitle(title);

		if (productLineConfig != null) {
			Company company = productLineConfig.getCompany();

			company.removeProductLine(line);
			return storeConfig(productLineConfig);
		}
		return false;
	}

	@Override
	public synchronized void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public ProductLine findProductLine(String line, String title) {
		ProductLineConfig productLineConfig = queryProductLineByTitle(title);

		if (productLineConfig != null) {
			return productLineConfig.getCompany().findProductLine(line);
		}
		return null;
	}

	@Override
	public void initialize() throws InitializationException {
		for (ProductLineConfig productLine : ProductLineConfig.values()) {
			initializeConfig(productLine);
		}
		m_metricProductLines = buildMetricProductLines();
	}

	private void initializeConfig(ProductLineConfig productLine) {
		try {
			Config config = m_configDao.findByName(productLine.getConfigName(), ConfigEntity.READSET_FULL);
			String content = config.getContent();

			productLine.setConfigId(config.getId());
			productLine.setCompany(DefaultSaxParser.parse(content));
			productLine.setModifyTime(config.getModifyDate().getTime());
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(productLine.getConfigName());
				Config config = m_configDao.createLocal();

				config.setName(productLine.getConfigName());
				config.setContent(content);
				m_configDao.insert(config);
				productLine.setConfigId(config.getId());
				productLine.setCompany(DefaultSaxParser.parse(content));
				productLine.setModifyTime(new Date().getTime());
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (productLine.getCompany() == null) {
			productLine.setCompany(new Company());
		}
	}

	public boolean insertIfNotExsit(String product, String domain) {
		ProductLineConfig productLineConfig = queryProductLineConfig(product, domain);
		Company company = productLineConfig.getCompany();

		if (company != null) {
			ProductLine productLine = company.getProductLines().get(product);

			if (productLine == null) {
				productLine = new ProductLine();

				productLine.setId(product);
				productLine.setTitle(product);
				productLine.addDomain(new Domain(domain));
				company.addProductLine(productLine);
				return storeConfig(productLineConfig);
			} else {
				Map<String, Domain> domains = productLine.getDomains();

				if (domains.containsKey(domain)) {
					return true;
				} else {
					domains.put(domain, new Domain(domain));

					return storeConfig(productLineConfig);
				}
			}
		}
		return false;
	}

	public Pair<Boolean, String> insertProductLine(ProductLine line, String[] domains, String title) {
		boolean flag = false;
		String duplicateDomains = "";
		ProductLineConfig productLineConfig = queryProductLineByTitle(title);

		if (productLineConfig != null) {
			duplicateDomains = buildDomainInfo(productLineConfig, line, domains);

			productLineConfig.getCompany().addProductLine(line);
			flag = storeConfig(productLineConfig);
		}

		return new Pair<Boolean, String>(flag, duplicateDomains);
	}

	public Map<String, ProductLine> queryAllProductLines() {
		Map<String, ProductLine> productLines = new TreeMap<String, ProductLine>();

		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			productLines.putAll(queryProductLines(productLineConfig));
		}
		return sortProductLineByOrder(productLines);
	}

	public Map<String, ProductLine> queryDatabaseProductLines() {
		return queryProductLines(ProductLineConfig.DATABASE);
	}

	public Map<String, ProductLine> queryApplicationProductLines() {
		return queryProductLines(ProductLineConfig.APPLICATION);
	}

	public List<String> queryDomainsByProductLine(String productLine, ProductLineConfig productLineConfig) {
		List<String> domains = new ArrayList<String>();
		ProductLine line = productLineConfig.getCompany().findProductLine(productLine);

		if (line != null) {
			for (Domain domain : line.getDomains().values()) {
				domains.add(domain.getId());
			}
		}
		return domains;
	}

	public Map<String, ProductLine> queryMetricProductLines() {
		return queryProductLines(ProductLineConfig.METRIC);
	}

	public Map<String, ProductLine> queryNetworkProductLines() {
		return queryProductLines(ProductLineConfig.NETWORK);
	}

	public String queryProductLineByDomain(String domain) {
		String productLine = m_metricProductLines.get(domain);

		return productLine;
	}

	public ProductLineConfig queryProductLine(String name) {
		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			ProductLine productLine = productLineConfig.getCompany().findProductLine(name);

			if (productLine != null) {
				return productLineConfig;
			}
		}
		return null;
	}

	public ProductLineConfig queryProductLineByTitle(String title) {
		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			if (productLineConfig.getTitle().equals(title)) {
				return productLineConfig;
			}
		}
		return null;
	}

	private ProductLineConfig queryProductLineConfig(String line, String domain) {
		if (ProductLineConfig.USER.isTypeOf(domain)) {
			return ProductLineConfig.USER;
		} else if (ProductLineConfig.NETWORK.isTypeOf(line)) {
			return ProductLineConfig.NETWORK;
		} else if (ProductLineConfig.SYSTEM.isTypeOf(line)) {
			return ProductLineConfig.SYSTEM;
		} else if (ProductLineConfig.DATABASE.isTypeOf(line)) {
			return ProductLineConfig.DATABASE;
		} else if (ProductLineConfig.CDN.isTypeOf(line)) {
			return ProductLineConfig.CDN;
		} else {
			return ProductLineConfig.METRIC;
		}
	}

	private Map<String, ProductLine> queryProductLines(ProductLineConfig productLineConfig) {
		Map<String, ProductLine> productLines = new LinkedHashMap<String, ProductLine>();

		for (ProductLine line : productLineConfig.getCompany().getProductLines().values()) {
			String id = line.getId();
			if (id != null && id.length() > 0) {
				productLines.put(id, line);
			}
		}
		return productLines;
	}

	public String querySystemProductLine(String domain) {
		return ProductLineConfig.SYSTEM.getPrefix().get(0) + domain;
	}

	public Map<String, ProductLine> querySystemProductLines() {
		return queryProductLines(ProductLineConfig.SYSTEM);
	}

	public Map<String, List<ProductLine>> queryTypeProductLines() {
		Map<String, List<ProductLine>> productLines = new LinkedHashMap<String, List<ProductLine>>();

		for (ProductLineConfig e : ProductLineConfig.values()) {
			List<ProductLine> lst = new ArrayList<ProductLine>();

			for (ProductLine line : e.getCompany().getProductLines().values()) {
				String id = line.getId();

				if (StringUtils.isNotEmpty(id)) {
					lst.add(line);
				}
			}
			productLines.put(e.getTitle(), lst);
		}

		for (Entry<String, List<ProductLine>> entry : productLines.entrySet()) {
			List<ProductLine> value = entry.getValue();
			Collections.sort(value, new Comparator<ProductLine>() {

				@Override
				public int compare(ProductLine o1, ProductLine o2) {
					return (int) (o1.getOrder() * 100 - o2.getOrder() * 100);
				}
			});
		}

		return productLines;
	}

	public void refreshConfig() throws DalException, SAXException, IOException {
		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			Config config = m_configDao.findByName(productLineConfig.getConfigName(), ConfigEntity.READSET_FULL);
			long modifyTime = config.getModifyDate().getTime();

			synchronized (productLineConfig) {
				if (modifyTime > productLineConfig.getModifyTime()) {
					String content = config.getContent();
					Company company = DefaultSaxParser.parse(content);

					productLineConfig.setCompany(company);
					productLineConfig.setModifyTime(modifyTime);

					m_metricProductLines = buildMetricProductLines();
					m_logger.info("product line [" + productLineConfig.getTitle() + "] config refresh done!");
				}
			}
		}
	}

	private <K, V> Map<K, V> sortMap(Map<K, V> map, Comparator<Entry<K, V>> compator) {
		Map<K, V> result = new LinkedHashMap<K, V>();
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
		Collections.sort(entries, compator);
		for (Entry<K, V> entry : entries) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}

	private Map<String, ProductLine> sortProductLineByOrder(Map<String, ProductLine> productLines) {
		return sortMap(productLines, new Comparator<Map.Entry<String, ProductLine>>() {

			@Override
			public int compare(Entry<String, ProductLine> o1, Entry<String, ProductLine> o2) {
				return (int) (o1.getValue().getOrder() * 100 - o2.getValue().getOrder() * 100);
			}
		});
	}

	private boolean storeConfig(ProductLineConfig productLineConfig) {
		synchronized (productLineConfig) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(productLineConfig.getConfigId());
				config.setKeyId(productLineConfig.getConfigId());
				config.setName(productLineConfig.getConfigName());
				config.setContent(productLineConfig.getCompany().toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
				m_metricProductLines = buildMetricProductLines();
				return true;
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
	}
}