package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertType;

public class BusinessContactor extends ProductlineContactor {

	public static final String ID = AlertType.BUSINESS;

	@Override
	public String getId() {
		return ID;
	}

}
