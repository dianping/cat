package com.dianping.cat.system.alarm.template;

import com.dianping.cat.system.alarm.exception.ExceptionDataEntity;

public interface ThresholdRule {

	public String getConnectUrl();

	public void addData(ExceptionDataEntity data);

	public String match();

}
