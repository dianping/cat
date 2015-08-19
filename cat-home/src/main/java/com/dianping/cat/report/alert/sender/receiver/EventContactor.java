package com.dianping.cat.report.alert.sender.receiver;

import com.dianping.cat.report.alert.AlertType;

public class EventContactor extends ProjectContactor {

	public static final String ID = AlertType.Event.getName();

	@Override
	public String getId() {
		return ID;
	}

}
