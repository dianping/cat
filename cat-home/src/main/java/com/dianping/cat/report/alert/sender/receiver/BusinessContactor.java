package com.dianping.cat.report.alert.sender.receiver;

import com.dianping.cat.report.alert.AlertType;

public class BusinessContactor extends ProjectContactor {

	public static final String ID = AlertType.Business.getName();

	@Override
	public String getId() {
		return ID;
	}

}
