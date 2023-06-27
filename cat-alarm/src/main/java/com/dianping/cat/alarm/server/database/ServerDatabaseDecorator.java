package com.dianping.cat.alarm.server.database;

import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.decorator.Decorator;

public class ServerDatabaseDecorator extends Decorator {

	public static final String ID = AlertType.SERVER_DATABASE.getName();

	@Override
	public String generateContent(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT 数据库告警] [设备: ").append(alert.getGroup()).append("] [监控项: ").append(alert.getMetric())
		      .append("]<br/>").append(alert.getContent());
		return sb.toString();
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT 数据库告警]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}
}
