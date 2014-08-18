package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertType;

public class NetworkContactor extends ProductlineContactor {

	public static final String ID = AlertType.Network.getName();

	@Override
	public String getId() {
		return ID;
	}

}
