package com.dianping.cat.system.config;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Files;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.dianping.cat.home.alert.type.entity.AlertType;
import com.dianping.cat.home.alert.type.entity.Category;
import com.dianping.cat.home.alert.type.entity.Domain;
import com.dianping.cat.home.alert.type.entity.Type;
import com.dianping.cat.home.alert.type.transform.DefaultSaxParser;

public class AlertTypeManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	private int m_configId;

	private AlertType m_config;

	private static final String CONFIG_NAME = "alertType";

	private static final String DEFAULT_TYPE = "default";

	public AlertType getAlertType() {
		return m_config;
	}

	public Type getType(String categoryName, String domainName, String typeName) {
		try {
			Category category = m_config.findCategory(categoryName);
			Domain domain = category.findDomain(domainName);
			if (domain == null) {
				domain = category.findDomain(DEFAULT_TYPE);
			}

			Type type = domain.findType(typeName);
			if (type == null) {
				type = generateDefaultType();
			}

			return type;
		} catch (Exception ex) {
			return generateDefaultType();
		}
	}

	private Type generateDefaultType() {
		Type type = new Type();

		type.setSendMail(true);
		type.setSendWeixin(true);
		type.setSendSms(false);

		return type;
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
				String content = Files.forIO().readFrom(
				      this.getClass().getResourceAsStream("/config/default-alert-type.xml"), "utf-8");
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
			m_config = new AlertType();
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

}
