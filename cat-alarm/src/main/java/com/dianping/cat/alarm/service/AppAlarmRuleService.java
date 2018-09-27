package com.dianping.cat.alarm.service;

import java.util.List;
import java.util.Map;

import com.dianping.cat.alarm.AppAlarmRule;

public interface AppAlarmRuleService {

	public boolean delete(int id);

	public boolean deleteByCommand(int id);

	public boolean insert(AppAlarmRule rule);

	public Map<String, List<AppAlarmRuleInfo>> queryAllRules();

	public AppAlarmRuleInfo queryById(int id);

	public List<AppAlarmRuleInfo> queryRules(String app);

	public void refresh();

	public boolean update(AppAlarmRule rule);

}
