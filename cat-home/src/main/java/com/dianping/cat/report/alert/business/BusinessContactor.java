package com.dianping.cat.report.alert.business;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;

public class BusinessContactor extends ProjectContactor {

	public static final String ID = AlertType.Business.getName();

	@Override
	public String getId() {
		return ID;
	}
}
