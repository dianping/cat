package com.dianping.cat.alarm.app.crash;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.crash.entity.CrashAlarmRule;
import com.dianping.cat.alarm.crash.entity.ExceptionLimit;
import com.dianping.cat.alarm.crash.transform.DefaultSaxParser;
import com.dianping.cat.config.content.ContentFetcher;
import com.dianping.cat.core.config.Config;
import com.dianping.cat.core.config.ConfigDao;
import com.dianping.cat.core.config.ConfigEntity;
import com.site.lookup.util.StringUtils;

@Named
public class CrashRuleConfigManager implements Initializable {

	@Inject
	private ConfigDao m_configDao;

	@Inject
	private ContentFetcher m_fetcher;

	private int m_configId;

	private CrashAlarmRule m_crashAlarmRule;

	private static final String CONFIG_NAME = "crash-alarm-rule";

	public static final String SPLITTER = ":";

	public boolean deleteExceptionLimit(String ruleId) {
		m_crashAlarmRule.removeExceptionLimit(ruleId);

		return storeConfig();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Config config = m_configDao.findByName(CONFIG_NAME, ConfigEntity.READSET_FULL);
			String content = config.getContent();
			m_configId = config.getId();
			m_crashAlarmRule = DefaultSaxParser.parse(content);
		} catch (DalNotFoundException e) {
			try {
				String content = m_fetcher.getConfigContent(CONFIG_NAME);
				Config config = m_configDao.createLocal();

				config.setName(CONFIG_NAME);
				config.setContent(content);
				m_configDao.insert(config);

				m_configId = config.getId();
				m_crashAlarmRule = DefaultSaxParser.parse(content);
			} catch (Exception ex) {
				Cat.logError(ex);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		if (m_crashAlarmRule == null) {
			m_crashAlarmRule = new CrashAlarmRule();
		}
	}

	public boolean insertExceptionLimit(ExceptionLimit limit) {
		String id = limit.getId();

		if (StringUtils.isEmpty(id)) {
			id = buildRuleId(limit.getAppId(), limit.getPlatform(), limit.getModule());

			limit.setId(id);
		}

		m_crashAlarmRule.getExceptionLimits().put(id, limit);

		return storeConfig();
	}

	private String buildRuleId(int appId, String platform, String module) {
		StringBuilder sb = new StringBuilder();

		sb.append(appId);
		sb.append(SPLITTER);
		sb.append(platform);
		sb.append(SPLITTER);
		sb.append(module);

		return sb.toString();
	}

	public List<ExceptionLimit> queryAllExceptionLimits() {
		return new ArrayList<ExceptionLimit>(m_crashAlarmRule.getExceptionLimits().values());
	}

	public ExceptionLimit queryExceptionLimit(String ruleId) {
		return m_crashAlarmRule.findExceptionLimit(ruleId);
	}

	private boolean storeConfig() {
		synchronized (this) {
			try {
				Config config = m_configDao.createLocal();

				config.setId(m_configId);
				config.setKeyId(m_configId);
				config.setName(CONFIG_NAME);
				config.setContent(m_crashAlarmRule.toString());
				m_configDao.updateByPK(config, ConfigEntity.UPDATESET_FULL);
			} catch (Exception e) {
				Cat.logError(e);
				return false;
			}
		}
		return true;
	}

}
