package com.dianping.cat.report.alert.event;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;

public class EventContactor extends ProjectContactor {

	public static final String ID = AlertType.Event.getName();

	@Override
	public String getId() {
		return ID;
	}

}
