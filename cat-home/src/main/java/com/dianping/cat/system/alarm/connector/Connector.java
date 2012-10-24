package com.dianping.cat.system.alarm.connector;

import com.dianping.cat.system.alarm.threshold.ThresholdDataEntity;

public interface Connector {

	public ThresholdDataEntity fetchAlarmData(String url);
}
