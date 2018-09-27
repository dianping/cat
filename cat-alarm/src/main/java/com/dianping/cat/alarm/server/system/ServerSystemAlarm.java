package com.dianping.cat.alarm.server.system;

import com.dianping.cat.alarm.server.AbstractServerAlarm;
import com.dianping.cat.alarm.spi.AlertType;

public class ServerSystemAlarm extends AbstractServerAlarm {

	public static final String ID = AlertType.SERVER_SYSTEM.getName();

	@Override
	public String getCategory() {
		return "system";
	}

	@Override
	public String getID() {
		return ID;
	}

}
