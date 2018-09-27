package com.dianping.cat.alarm.service;

import java.util.List;
import java.util.Map;

import com.dianping.cat.alarm.ServerAlarmRule;

public interface ServerAlarmRuleService {

	public boolean delete(ServerAlarmRule rule);

	public boolean insert(ServerAlarmRule rule);

	public Map<String, List<ServerAlarmRule>> queryAllRules();

	public List<ServerAlarmRule> queryRules(String category);

	public ServerAlarmRule queryById(int id);

	public boolean update(ServerAlarmRule rule);

	public void refresh();

}
