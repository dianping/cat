package com.dianping.cat.alarm.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.AppAlarmRule;
import com.dianping.cat.alarm.AppAlarmRuleDao;
import com.dianping.cat.alarm.AppAlarmRuleEntity;
import com.dianping.cat.alarm.app.AppAlarmRuleParamBuilder;
import com.dianping.cat.alarm.service.AppAlarmRuleInfo;
import com.dianping.cat.alarm.service.AppAlarmRuleService;

@Named(type = AppAlarmRuleService.class)
public class AppAlarmRuleServiceImpl implements AppAlarmRuleService, Initializable {

	@Inject
	private AppAlarmRuleDao m_dao;

	private Map<String, List<AppAlarmRuleInfo>> m_alarmRules = new ConcurrentHashMap<String, List<AppAlarmRuleInfo>>();

	@Override
	public boolean delete(int id) {
		try {
			AppAlarmRule rule = new AppAlarmRule();

			rule.setId(id);
			m_dao.deleteByPK(rule);
			refresh();
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

	@Override
	public void initialize() throws InitializationException {
		refresh();
	}

	@Override
	public boolean insert(AppAlarmRule rule) {
		try {
			m_dao.insert(rule);
			refresh();
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

	@Override
	public Map<String, List<AppAlarmRuleInfo>> queryAllRules() {
		return m_alarmRules;
	}

	@Override
	public AppAlarmRuleInfo queryById(int id) {
		try {
			AppAlarmRule entity = m_dao.findByPK(id, AppAlarmRuleEntity.READSET_FULL);

			return new AppAlarmRuleInfo(entity);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	public List<AppAlarmRuleInfo> queryRules(String app) {
		return m_alarmRules.get(app);
	}

	@Override
	public void refresh() {
		try {
			Map<String, List<AppAlarmRuleInfo>> alarmRules = new ConcurrentHashMap<String, List<AppAlarmRuleInfo>>();
			List<AppAlarmRule> entities = m_dao.findAll(AppAlarmRuleEntity.READSET_FULL);

			for (AppAlarmRule entity : entities) {
				String category = entity.getApp();
				List<AppAlarmRuleInfo> rules = alarmRules.get(category);

				if (rules == null) {
					rules = new ArrayList<AppAlarmRuleInfo>();

					alarmRules.put(category, rules);
				}
				rules.add(new AppAlarmRuleInfo(entity));
			}
			m_alarmRules = alarmRules;
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	@Override
	public boolean update(AppAlarmRule rule) {
		try {
			m_dao.updateByPK(rule, AppAlarmRuleEntity.UPDATESET_FULL);
			refresh();
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

	@Override
	public boolean deleteByCommand(int id) {
		for (Entry<String, List<AppAlarmRuleInfo>> entry : m_alarmRules.entrySet()) {
			for (AppAlarmRuleInfo info : entry.getValue()) {
				try {
					int commandId = Integer.parseInt(info.getRule().getDynamicAttribute(AppAlarmRuleParamBuilder.COMMAND));

					if (commandId == id) {
						return delete(info.getEntity().getId());
					}
				} catch (NumberFormatException e) {
					Cat.logError(e);
				}
			}
		}
		return false;
	}
}
