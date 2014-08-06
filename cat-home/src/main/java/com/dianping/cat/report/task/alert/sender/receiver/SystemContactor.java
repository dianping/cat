package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertType;

public class SystemContactor extends ProductlineContactor {

	public static final String ID = AlertType.SYSTEM;

	@Override
	public String getId() {
		return ID;
	}

}
