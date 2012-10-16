package com.dianping.cat.system.alarm.connector;

import com.dianping.cat.system.alarm.entity.AlarmData;

public interface Connector {

	public AlarmData fetchAlarmData(String url);
}
