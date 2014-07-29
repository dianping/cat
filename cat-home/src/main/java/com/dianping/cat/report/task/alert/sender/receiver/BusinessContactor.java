package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertConstants;

public class BusinessContactor extends ProductlineContactor {

	public static final String ID = AlertConstants.BUSINESS;

	@Override
	public String getId() {
		return ID;
	}

}
