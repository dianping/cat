package com.dianping.cat.alarm.server.system;

import com.dianping.cat.alarm.server.TagSplitHelper;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.decorator.Decorator;

public class ServerSystemDecorator extends Decorator {

	public static final String ID = AlertType.SERVER_SYSTEM.getName();

	@Override
	public String generateContent(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT 系统告警] [项目: ").append(TagSplitHelper.queryDomain(alert.getGroup())).append("] [机器：")
		      .append(TagSplitHelper.queryByKey(alert.getGroup(), "endPoint")).append("] [监控项: ")
		      .append(alert.getMetric()).append("]<br/>").append(alert.getContent());

		return sb.toString();
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT 系统告警]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}
}
