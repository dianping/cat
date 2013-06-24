package com.dianping.cat.consumer.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.dianping.cat.consumer.core.config.Config;
import com.dianping.cat.consumer.core.config.ConfigDao;
import com.dianping.cat.consumer.core.config.ConfigEntity;

public class ProductLineConfigManager implements Initializable, LogEnabled {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private Company m_company;

	private Map<String, String> m_domainToProductLines = new HashMap<String, String>();

	private long m_modifyTime;

	private Logger m_logger;

	private static final String CONFIG_NAME = "productLineConfig";

	public Company getCompany() {
		synchronized (m_company) {
			return m_company;
		}
	}

	public boolean deleteProductLine(String line) {
		getCompany().removeProductLine(line);
		return storeConfig();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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
				m_modifyTime = config.getModifyDate().getTime();
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (getCompany() == null) {
			m_company = new Company();
		}
		m_domainToProductLines =buildDomainToProductLines();
	}

	public boolean insertProductLine(ProductLine line, String[] domains) {
		getCompany().removeProductLine(line.getId());
		getCompany().addProductLine(line);

		for (String domain : domains) {
			line.addDomain(new Domain(domain));
		}
		return storeConfig();
	}

	public String queryProductLineByDomain(String domain) {
		String productLine = m_domainToProductLines.get(domain);

		return productLine == null ? "Default" : productLine;
	}

	public List<String> queryProductLineDomains(String productLine) {
		List<String> domains = new ArrayList<String>();
		ProductLine line = getCompany().findProductLine(productLine);

		if (line != null) {
			for (Domain domain : line.getDomains().values()) {
				domains.add(domain.getId());
			}
		}
		return domains;
	}
	
	public Map<String, ProductLine> queryProductLines() {
		Map<String, ProductLine> productLines = new TreeMap<String, ProductLine>();

		for (ProductLine line : getCompany().getProductLines().values()) {
			productLines.put(line.getId(), line);
		}
		return sortMap(productLines, new Comparator<Map.Entry<String, ProductLine>>() {

			@Override
			public int compare(Entry<String, ProductLine> o1, Entry<String, ProductLine> o2) {
				return (int) (o1.getValue().getOrder() * 100 - o2.getValue().getOrder() * 100);
			}
		});
	}

	private Map<String, String>  buildDomainToProductLines(){
		Map<String, ProductLine> productLines = getCompany().getProductLines();
		Map<String, String> domainToProductLines = new HashMap<String, String>();

		for (ProductLine product : productLines.values()) {
			for (Domain domain : product.getDomains().values()) {
				domainToProductLines.put(domain.getId(), product.getId());
			}
		}
		return domainToProductLines;
	}
	private boolean storeConfig() {
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

		m_domainToProductLines =buildDomainToProductLines();
		return true;
	}
	public  <K, V> Map<K, V> sortMap(Map<K, V> map, Comparator<Entry<K, V>> compator) {
		Map<K, V> result = new LinkedHashMap<K, V>();
		List<Entry<K, V>> entries = new ArrayList<Entry<K, V>>(map.entrySet());
		Collections.sort(entries, compator);
		for (Entry<K, V> entry : entries) {
			result.put(entry.getKey(), entry.getValue());
		}

		return result;
	}
	
	public void refreshProductLineConfig() throws DalException, SAXException, IOException {
      Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
      long modifyTime = config.getModifyDate().getTime();

      if (modifyTime > m_modifyTime) {
      	String content = config.getContent();

      	synchronized (m_company) {
      		m_company = DefaultSaxParser.parse(content);
      	}

      	m_modifyTime = modifyTime;
      	m_logger.info("product line config refresh done!");
      }
   }

}
