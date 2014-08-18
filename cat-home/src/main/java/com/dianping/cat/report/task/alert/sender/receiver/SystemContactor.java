package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertType;

public class SystemContactor extends ProductlineContactor {

	public static final String ID = AlertType.System.getName();

	@Override
	public String getId() {
		return ID;
	}

}
