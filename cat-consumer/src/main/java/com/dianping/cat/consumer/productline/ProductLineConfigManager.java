package com.dianping.cat.consumer.productline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
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
	private ContentFetcher m_getter;

	private Logger m_logger;

	private Map<String, String> m_domainToProductLines = new HashMap<String, String>();

	public static final String CONFIG_NAME = "productLineConfig";

	private Company buildDefaultConfig(ProductLineConfig productLine) throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		String content = config.getContent();
		Company company = DefaultSaxParser.parse(content);
		Company c = new Company();

		switch (productLine) {
		case METRIC_PRODUCTLINE:
			for (ProductLine p : company.getProductLines().values()) {
				if (p.getMetricDashboard()) {
					c.addProductLine(p);
				}
			}
			break;
		case APPLICATION_PRODUCTLINE:
			for (ProductLine p : company.getProductLines().values()) {
				if (p.getApplicationDashboard()) {
					c.addProductLine(p);
				}
			}
			break;
		case NETWORK_PRODUCTLINE:
			for (ProductLine p : company.getProductLines().values()) {
				if (p.getNetworkDashboard()) {
					c.addProductLine(p);
				}
			}
			break;
		case SYSTEM_PRODUCTLINE:
			for (ProductLine p : company.getProductLines().values()) {
				if (p.getSystemMonitorDashboard()) {
					c.addProductLine(p);
				}
			}
			break;
		case USER_PRODUCTLINE:
			for (ProductLine p : company.getProductLines().values()) {
				if (p.getUserMonitorDashboard()) {
					c.addProductLine(p);
				}
			}
			break;
		case DATABASE_PRODUCTLINE:
			for (ProductLine p : company.getProductLines().values()) {
				if (p.getDatabaseMonitorDashboard()) {
					c.addProductLine(p);
				}
			}
			break;
		}
		return c;
	}

	public void buildDefaultDashboard(ProductLine productLine, ProductLineConfig productLineConfig) {
		switch (productLineConfig) {
		case USER_PRODUCTLINE:
			productLine.setUserMonitorDashboard(true);
			break;
		case NETWORK_PRODUCTLINE:
			productLine.setNetworkDashboard(true);
			break;
		case SYSTEM_PRODUCTLINE:
			productLine.setSystemMonitorDashboard(true);
			break;
		case DATABASE_PRODUCTLINE:
			productLine.setDatabaseMonitorDashboard(true);
			break;
		default:
			productLine.setMetricDashboard(true);
		}
	}

	private String buildDomainInfo(ProductLineConfig productLineConfig, ProductLine productline, String[] domainIds) {
		Set<String> domains = new HashSet<String>();
		String id = productline.getId();
		String duplicateDomains = "";

		if (ProductLineConfig.METRIC_PRODUCTLINE.equals(productLineConfig)
		      || ProductLineConfig.APPLICATION_PRODUCTLINE.equals(productLineConfig)) {
			for (ProductLineConfig config : ProductLineConfig.values()) {
				for (ProductLine product : config.getCompany().getProductLines().values()) {
					if (!product.getId().equals(id)) {
						for (Domain domain : product.getDomains().values()) {
							domains.add(domain.getId());
						}
					}
				}
			}
			for (String domain : domainIds) {
				if (domains.contains(domain)) {
					duplicateDomains += domain + ", ";
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

	private Map<String, String> buildDomainToProductLines() {
		Map<String, String> domainToProductLines = new HashMap<String, String>();

		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			domainToProductLines.putAll(productLineConfig.getDomainToProductLines());
		}
		return domainToProductLines;
	}

	public boolean deleteProductLine(String line, String title) {
		boolean flag = true;
		ProductLineConfig productLineConfig = queryProductLineByTitle(title);

		if (productLineConfig != null) {
			Company company = productLineConfig.getCompany();

			switch (productLineConfig) {
			case METRIC_PRODUCTLINE:
				ProductLine product = ProductLineConfig.APPLICATION_PRODUCTLINE.getCompany().findProductLine(line);

				if (product != null) {
					product.setMetricDashboard(false);
				}
				flag = storeConfig(ProductLineConfig.APPLICATION_PRODUCTLINE);
				break;
			case APPLICATION_PRODUCTLINE:
				product = ProductLineConfig.METRIC_PRODUCTLINE.getCompany().findProductLine(line);

				if (product != null) {
					product.setApplicationDashboard(false);
				}
				flag = storeConfig(ProductLineConfig.METRIC_PRODUCTLINE);
				break;
			default:
				break;
			}
			company.removeProductLine(line);
			return storeConfig(productLineConfig) && flag;
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
		m_domainToProductLines = buildDomainToProductLines();
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
				Company company = buildDefaultConfig(productLine);
				Config config = m_configDao.createLocal();

				config.setName(productLine.getConfigName());
				config.setContent(company.toString());
				m_configDao.insert(config);
				productLine.setConfigId(config.getId());
				productLine.setCompany(company);
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
				buildDefaultDashboard(productLine, productLineConfig);
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
		boolean flag = true;
		String duplicateDomains = "";
		ProductLineConfig productLineConfig = queryProductLineByTitle(title);

		switch (productLineConfig) {
		case METRIC_PRODUCTLINE:
			if (line.getApplicationDashboard()) {
				ProductLineConfig.APPLICATION_PRODUCTLINE.getCompany().removeProductLine(line.getId());
				ProductLineConfig.APPLICATION_PRODUCTLINE.getCompany().addProductLine(line);
				flag = storeConfig(ProductLineConfig.APPLICATION_PRODUCTLINE);
			}
			break;
		case APPLICATION_PRODUCTLINE:
			if (line.getMetricDashboard()) {
				ProductLineConfig.METRIC_PRODUCTLINE.getCompany().removeProductLine(line.getId());
				ProductLineConfig.METRIC_PRODUCTLINE.getCompany().addProductLine(line);
				flag = storeConfig(ProductLineConfig.METRIC_PRODUCTLINE);
			}
			break;
		case USER_PRODUCTLINE:
			line.setUserMonitorDashboard(true);
			break;
		case DATABASE_PRODUCTLINE:
			line.setDatabaseMonitorDashboard(true);
			break;
		case NETWORK_PRODUCTLINE:
			line.setNetworkDashboard(true);
			break;
		case SYSTEM_PRODUCTLINE:
			line.setSystemMonitorDashboard(true);
			break;
		}
		duplicateDomains = buildDomainInfo(productLineConfig, line, domains);
		productLineConfig.getCompany().removeProductLine(line.getId());
		productLineConfig.getCompany().addProductLine(line);

		return new Pair<Boolean, String>(storeConfig(productLineConfig) && flag, duplicateDomains);
	}

	public Map<String, ProductLine> queryAllProductLines() {
		Map<String, ProductLine> productLines = new TreeMap<String, ProductLine>();

		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			productLines.putAll(queryProductLines(productLineConfig));
		}
		return sortProductLineByOrder(productLines);
	}

	public Map<String, ProductLine> queryDatabases() {
		return queryProductLines(ProductLineConfig.DATABASE_PRODUCTLINE);
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
		return queryProductLines(ProductLineConfig.METRIC_PRODUCTLINE);
	}

	public Map<String, ProductLine> queryNetworkProductLines() {
		return queryProductLines(ProductLineConfig.NETWORK_PRODUCTLINE);
	}

	public ProductLine queryProductLine(String id) {
		Pair<ProductLineConfig, ProductLine> pair = queryProductLineConfig(id);

		return pair.getValue();
	}

	public String queryProductLineByDomain(String domain) {
		String productLine = m_domainToProductLines.get(domain);

		return productLine == null ? "Default" : productLine;
	}

	public Pair<ProductLineConfig, ProductLine> queryProductLineConfig(String name) {
		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			ProductLine productLine = productLineConfig.getCompany().findProductLine(name);

			if (productLine != null) {
				return new Pair<ProductLineConfig, ProductLine>(productLineConfig, productLine);
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
		if (ProductLineConfig.USER_PRODUCTLINE.isTypeOf(domain)) {
			return ProductLineConfig.USER_PRODUCTLINE;
		} else if (ProductLineConfig.NETWORK_PRODUCTLINE.isTypeOf(line)) {
			return ProductLineConfig.NETWORK_PRODUCTLINE;
		} else if (ProductLineConfig.SYSTEM_PRODUCTLINE.isTypeOf(line)) {
			return ProductLineConfig.SYSTEM_PRODUCTLINE;
		} else if (ProductLineConfig.DATABASE_PRODUCTLINE.isTypeOf(line)) {
			return ProductLineConfig.DATABASE_PRODUCTLINE;
		} else {
			return ProductLineConfig.METRIC_PRODUCTLINE;
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
		return ProductLineConfig.SYSTEM_PRODUCTLINE.getPrefix().get(0) + domain;
	}

	public Map<String, ProductLine> querySystemProductLines() {
		return queryProductLines(ProductLineConfig.SYSTEM_PRODUCTLINE);
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

	public void refreshProductLineConfig() throws DalException, SAXException, IOException {
		for (ProductLineConfig productLineConfig : ProductLineConfig.values()) {
			Config config = m_configDao.findByName(productLineConfig.getConfigName(), ConfigEntity.READSET_FULL);
			long modifyTime = config.getModifyDate().getTime();

			synchronized (productLineConfig) {
				if (modifyTime > productLineConfig.getModifyTime()) {
					String content = config.getContent();
					Company company = DefaultSaxParser.parse(content);

					productLineConfig.setCompany(company);
					productLineConfig.setModifyTime(modifyTime);
					m_logger.info("product line [" + productLineConfig.getTitle() + "] config refresh done!");
				}
			}
		}
	}

	public <K, V> Map<K, V> sortMap(Map<K, V> map, Comparator<Entry<K, V>> compator) {
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
				m_domainToProductLines = buildDomainToProductLines();
				return true;
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
	}
}