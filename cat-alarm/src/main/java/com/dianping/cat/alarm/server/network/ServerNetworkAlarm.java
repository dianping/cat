package com.dianping.cat.alarm.server.network;

import com.dianping.cat.alarm.server.AbstractServerAlarm;
import com.dianping.cat.alarm.spi.AlertType;

public class ServerNetworkAlarm extends AbstractServerAlarm {

	public static final String ID = AlertType.SERVER_NETWORK.getName();

	@Override
	public String getCategory() {
		return "network";
	}

	@Override
	public String getID() {
		return ID;
	}

}
