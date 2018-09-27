package com.dianping.cat.alarm.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.alarm.ServerAlarmRule;
import com.dianping.cat.alarm.ServerAlarmRuleDao;
import com.dianping.cat.alarm.ServerAlarmRuleEntity;
import com.dianping.cat.alarm.service.ServerAlarmRuleService;

@Named(type = ServerAlarmRuleService.class)
public class ServerAlarmRuleServiceImpl implements ServerAlarmRuleService, Initializable {

	@Inject
	private ServerAlarmRuleDao m_dao;

	private Map<String, List<ServerAlarmRule>> m_alarmRules = new ConcurrentHashMap<String, List<ServerAlarmRule>>();

	@Override
	public boolean delete(ServerAlarmRule rule) {
		try {
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
	public boolean insert(ServerAlarmRule rule) {
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
	public Map<String, List<ServerAlarmRule>> queryAllRules() {
		return m_alarmRules;
	}

	@Override
	public ServerAlarmRule queryById(int id) {
		try {
			return m_dao.findByPK(id, ServerAlarmRuleEntity.READSET_FULL);
		} catch (DalNotFoundException e) {
			// ignore
		} catch (DalException e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	public List<ServerAlarmRule> queryRules(String category) {
		List<ServerAlarmRule> rules = m_alarmRules.get(category);

		if (rules == null) {
			rules = new ArrayList<ServerAlarmRule>();
		}

		return rules;
	}

	@Override
	public void refresh() {
		try {
			Map<String, List<ServerAlarmRule>> alarmRules = new ConcurrentHashMap<String, List<ServerAlarmRule>>();
			List<ServerAlarmRule> entities = m_dao.findAll(ServerAlarmRuleEntity.READSET_FULL);

			for (ServerAlarmRule entity : entities) {
				String category = entity.getCategory();
				List<ServerAlarmRule> rules = alarmRules.get(category);

				if (rules == null) {
					rules = new ArrayList<ServerAlarmRule>();

					alarmRules.put(category, rules);
				}
				rules.add(entity);
			}
			m_alarmRules = alarmRules;
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	@Override
	public boolean update(ServerAlarmRule rule) {
		try {
			m_dao.updateByPK(rule, ServerAlarmRuleEntity.UPDATESET_FULL);
			refresh();
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

}
