package com.dianping.cat.report.page.dependency.config;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.home.dependency.format.entity.ProductLine;
import com.dianping.cat.home.dependency.format.entity.TopoGraphFormatConfig;
import com.dianping.cat.home.dependency.format.transform.DefaultSaxParser;

public class TopoGraphFormatConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private TopoGraphFormatConfig m_config;

	private static final String CONFIG_NAME = "topoGraphFormat";

	public TopoGraphFormatConfig getConfig() {
		return m_config;
	}

	@Override
	public void initialize() throws InitializationException {
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
			m_config = new TopoGraphFormatConfig();
		}
	}

	public boolean insert(String xml) {
		try {
			m_config = DefaultSaxParser.parse(xml);
			return storeConfig();
		} catch (Exception e) {
			Cat.logError(e);
			return false;
		}
	}

	private boolean storeConfig() {
		synchronized (this) {
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

	public String buildFormatJson() {
		Map<String, Map<String, Integer>> map = new HashMap<String, Map<String, Integer>>();

		for (ProductLine productline : m_config.getProductLines()) {
			Map<String, Integer> p = new HashMap<String, Integer>();

			map.put(productline.getId(), p);
			p.put("colInside", productline.getColInside());
		}
		return new JsonBuilder().toJson(map);
	}
}