package com.dianping.cat.system.config;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.webres.helper.Files;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.core.config.Config;
import com.dianping.cat.consumer.core.config.ConfigDao;
import com.dianping.cat.consumer.core.config.ConfigEntity;
import com.dianping.cat.helper.MapUtils;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.company.entity.Company;
import com.dianping.cat.home.company.entity.Domain;
import com.dianping.cat.home.company.entity.ProductLine;
import com.dianping.cat.home.company.transform.DefaultSaxParser;

public class ProductLineConfigManager implements Initializable, LogEnabled {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private Company m_company;

	private Map<String, String> m_domainToProductLine = new HashMap<String, String>();

	private long m_modifyTime;

	private Logger m_logger;

	private static final String CONFIG_NAME = "productLineConfig";

	public boolean deleteProductLine(String line) {
		m_company.removeProductLine(line);
		return storeConfig();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public Company getCompany() {
		return m_company;
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
		if (m_company == null) {
			m_company = new Company();
		}
	}

	public boolean insertProductLine(ProductLine line, String[] domains) {
		m_company.removeProductLine(line.getId());
		m_company.addProductLine(line);

		for (String domain : domains) {
			line.addDomain(new Domain(domain));
		}
		return storeConfig();
	}

	public String queryProductLineByDomain(String domain) {
		String productLine = m_domainToProductLine.get(domain);

		return productLine == null ? "Default" : productLine;
	}

	public List<String> queryProductLineDomains(String productLine) {
		List<String> domains = new ArrayList<String>();
		ProductLine line = m_company.findProductLine(productLine);

		if (line != null) {
			for (Domain domain : line.getDomains().values()) {
				domains.add(domain.getId());
			}
		}
		return domains;
	}

	public Map<String, ProductLine> queryProductLines() {
		Map<String, ProductLine> productLines = new TreeMap<String, ProductLine>();

		for (ProductLine line : m_company.getProductLines().values()) {
			productLines.put(line.getId(), line);
		}
		return MapUtils.sortMap(productLines, new Comparator<Map.Entry<String, ProductLine>>() {

			@Override
			public int compare(Entry<String, ProductLine> o1, Entry<String, ProductLine> o2) {
				return (int) (o2.getValue().getOrder() * 100 - o1.getValue().getOrder() * 100);
			}
		});
	}

	private boolean storeConfig() {
		try {
			Config config = m_configDao.createLocal();
			config.setId(m_configId);
			config.setKeyId(m_configId);
			config.setName(CONFIG_NAME);
			config.setContent(m_company.toString());
			m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}

		Map<String, ProductLine> productLines = m_company.getProductLines();
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

						synchronized (m_company) {
							m_company = DefaultSaxParser.parse(content);
						}

						m_modifyTime = modifyTime;
						m_logger.info("product line config refresh done!");
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
