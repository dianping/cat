package com.dianping.cat.system.alarm.template;

import com.dianping.cat.system.alarm.entity.AlarmData;

public interface ThresholdRule {

	public String getConnectUrl();

	public void addData(AlarmData data);

	public String match();

}
