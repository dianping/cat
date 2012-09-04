package com.dianping.dog.alarm.rule.exception;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.dog.alarm.rule.AlarmStrategy;
import com.dianping.dog.alarm.rule.Level;
import com.dianping.dog.connector.RowData;
import com.dianping.dog.event.AlarmEvent;

public class ExceptionRule {

	private String m_domain;
	
	private Map<Level, List<AlarmStrategy>> m_alarmStrategies = new HashMap<Level, List<AlarmStrategy>>();

	private Map<Level, Integer> m_alarmThresholds = new HashMap<Level, Integer>();

	public int getByAlarmStrategy(Level level) {
		return 0;
	}

	public AlarmEvent apply(List<RowData> datas) {
		return null;
	}
	
	public ExceptionRule addBaseRuleDef(ExceptionRuleDef def) {
		return this;
	}

	public Map<Level, List<AlarmStrategy>> getCustomAlarmStrategies() {
		return m_alarmStrategies;
	}

	public Map<Level, Integer> get_customAlarmThresholds() {
		return m_alarmThresholds;
	}

	public String getDomain() {
   	return m_domain;
   }

	public void setDomain(String domain) {
   	m_domain = domain;
   }
}
