package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.sender.AlertConstants;

public class NetworkContactor extends ProductlineContactor {

	public static final String ID = AlertConstants.NETWORK;

	@Override
	public String getId() {
		return ID;
	}

}
