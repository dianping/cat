package com.dianping.cat.report.alert.sender.decorator;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertEntity;

public class StorageSQLDecorator extends Decorator {

	public static final String ID = AlertType.STORAGE_SQL.getName();

	@Override
	public String generateContent(AlertEntity alert) {
		return alert.getContent();
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT StorageSQL告警] [数据库: ").append(alert.getGroup()).append("] [监控项: ").append(alert.getMetric())
		      .append("]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}

}
