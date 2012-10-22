package com.dianping.cat.system.alarm.connector;

import com.dianping.cat.system.alarm.exception.ExceptionDataEntity;

public interface Connector {

	public ExceptionDataEntity fetchAlarmData(String url);
}
