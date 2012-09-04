package com.dianping.dog.alarm.rule.exception;

import java.util.HashMap;
import java.util.Map;

import com.dianping.dog.alarm.rule.AlarmStrategy;
import com.dianping.dog.alarm.rule.Level;
import com.sun.tools.javac.util.List;

public class ExceptionRuleDef {

	private Map<Level, List<AlarmStrategy>> m_alarmStrategies = new HashMap<Level, List<AlarmStrategy>>();

	private Map<Level, Integer> m_alarmThresholds = new HashMap<Level, Integer>();

	public Map<Level, List<AlarmStrategy>> getAlarmStrategies() {
   	return m_alarmStrategies;
   }

	public void setAlarmStrategies(Map<Level, List<AlarmStrategy>> alarmStrategies) {
   	m_alarmStrategies = alarmStrategies;
   }

	public Map<Level, Integer> getAlarmThresholds() {
   	return m_alarmThresholds;
   }

	public void setAlarmThresholds(Map<Level, Integer> alarmThresholds) {
   	m_alarmThresholds = alarmThresholds;
   }
	
}
