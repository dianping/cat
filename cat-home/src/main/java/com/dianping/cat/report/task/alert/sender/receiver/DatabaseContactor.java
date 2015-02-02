package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertType;

public class DatabaseContactor extends ProjectContactor {

	public static final String ID = AlertType.DataBase.getName();

	@Override
	public String getId() {
		return ID;
	}

}
