package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertType;

public class HeartbeatContactor extends ProjectContactor {

	public static final String ID = AlertType.HeartBeat.getName();

	@Override
	public String getId() {
		return ID;
	}

}
