package com.dianping.cat.alarm.server.database;

import com.dianping.cat.alarm.server.AbstractServerAlarm;
import com.dianping.cat.alarm.spi.AlertType;

public class ServerDatabaseAlarm extends AbstractServerAlarm {

	public static final String ID = AlertType.SERVER_DATABASE.getName();

	@Override
	public String getCategory() {
		return "database";
	}

	@Override
	public String getID() {
		return ID;
	}

}
