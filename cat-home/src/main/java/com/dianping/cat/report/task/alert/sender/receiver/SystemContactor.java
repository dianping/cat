package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.sender.AlertConstants;

public class SystemContactor extends ProductlineContactor {

	public static final String ID = AlertConstants.SYSTEM;

	@Override
	public String getId() {
		return ID;
	}

}
