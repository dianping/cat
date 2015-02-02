package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertType;

public class ExceptionContactor extends ProjectContactor {

	public static final String ID = AlertType.Exception.getName();

	@Override
	public String getId() {
		return ID;
	}

}
