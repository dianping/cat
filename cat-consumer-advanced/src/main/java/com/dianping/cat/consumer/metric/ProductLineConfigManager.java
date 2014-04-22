package com.dianping.cat.consumer.metric;

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

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;
import org.xml.sax.SAXException;

import com.dianping.cat.Cat;
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

	private int m_configId;

	private Company m_company;

	private Map<String, String> m_domainToProductLines = new HashMap<String, String>();

	private long m_modifyTime;

	private Logger m_logger;

	private static final String CONFIG_NAME = "productLineConfig";

	private Map<String, String> buildDomainToProductLines() {
		Map<String, ProductLine> productLines = getCompany().getProductLines();
		Map<String, String> domainToProductLines = new HashMap<String, String>();

		for (ProductLine product : productLines.values()) {
			for (Domain domain : product.getDomains().values()) {
				domainToProductLines.put(domain.getId(), product.getId());
			}
		}
		return domainToProductLines;
	}

	public boolean deleteProductLine(String line) {
		getCompany().removeProductLine(line);
		return storeConfig();
	}

	@Override
	public synchronized void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public Company getCompany() {
		synchronized (this) {
			return m_company;
		}
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();

			m_company = DefaultSaxParser.parse(content);
			m_configId = config.getId();
			m_modifyTime = config.getModifyDate().getTime();
		} catch (DalNotFoundException e) {
			try {
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-product-line-config.xml"), "utf-8");
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_company = DefaultSaxParser.parse(content);
				m_configId = config.getId();
				m_modifyTime = new Date().getTime();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_company == null) {
			m_company = new Company();
		}
		m_domainToProductLines = buildDomainToProductLines();
	}

	public boolean insertIfNotExsit(String line, String domain) {
		Company company = getCompany();

		if (company != null) {
			ProductLine productLine = company.getProductLines().get(line);

			if (productLine == null) {
				productLine = new ProductLine();
				productLine.setId(line);
				productLine.setTitle(line);
				productLine.addDomain(new Domain(domain));
				company.addProductLine(productLine);

				return storeConfig();
			} else {
				Map<String, Domain> domains = productLine.getDomains();

				if (domains.containsKey(domain)) {
					return true;
				} else {
					domains.put(domain, new Domain(domain));

					return storeConfig();
				}
			}
		} else {
			return false;
		}
	}

	public boolean insertProductLine(ProductLine line, String[] domains) {
		getCompany().removeProductLine(line.getId());
		getCompany().addProductLine(line);

		for (String domain : domains) {
			line.addDomain(new Domain(domain));
		}
		return storeConfig();
	}

	public Map<String, ProductLine> queryAllProductLines() {
		Map<String, ProductLine> productLines = new TreeMap<String, ProductLine>();

		for (ProductLine line : getCompany().getProductLines().values()) {
			String id = line.getId();
			if (id != null && id.length() > 0) {
				productLines.put(id, line);
			}
		}
		return sortProductLineByOrder(productLines);
	}

	public List<String> queryDomainsByProductLine(String productLine) {
		List<String> domains = new ArrayList<String>();
		ProductLine line = getCompany().findProductLine(productLine);

		if (line != null) {
			for (Domain domain : line.getDomains().values()) {
				domains.add(domain.getId());
			}
		}
		return domains;
	}

	public Map<String, ProductLine> queryMetricProductLines() {
		Map<String, ProductLine> productLines = new TreeMap<String, ProductLine>();

		for (ProductLine line : getCompany().getProductLines().values()) {
			String id = line.getId();
			if (id != null && id.length() > 0 && line.getMetricDashboard()) {
				productLines.put(id, line);
			}
		}
		return sortProductLineByOrder(productLines);
	}
	
	public Map<String, ProductLine> queryNetworkProductLines() {
		Map<String, ProductLine> productLines = new LinkedHashMap<String, ProductLine>();

		for (ProductLine line : getCompany().getProductLines().values()) {
			String id = line.getId();
			if (id != null && id.length() > 0 && line.getNetworkDashboard()) {
				productLines.put(id, line);
			}
		}
		return productLines;
	}

	public String queryProductLineByDomain(String domain) {
		String productLine = m_domainToProductLines.get(domain);

		return productLine == null ? "Default" : productLine;
	}

	public void refreshProductLineConfig() throws DalException, SAXException, IOException {
		Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
		long modifyTime = config.getModifyDate().getTime();

		synchronized (this) {
			if (modifyTime > m_modifyTime) {
				String content = config.getContent();
				Company company = DefaultSaxParser.parse(content);

				m_company = company;
				m_domainToProductLines = buildDomainToProductLines();
				m_modifyTime = modifyTime;
				m_logger.info("product line config refresh done!");
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

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(getCompany().toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
			m_domainToProductLines = buildDomainToProductLines();
		}
		return true;
	}

}