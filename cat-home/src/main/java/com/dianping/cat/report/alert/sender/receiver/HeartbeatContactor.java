package com.dianping.cat.report.alert.sender.receiver;

import com.dianping.cat.report.alert.AlertType;

public class HeartbeatContactor extends ProjectContactor {

	public static final String ID = AlertType.HeartBeat.getName();

	@Override
	public String getId() {
		return ID;
	}

}
