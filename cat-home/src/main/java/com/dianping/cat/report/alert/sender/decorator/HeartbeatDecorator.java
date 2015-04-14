package com.dianping.cat.report.alert.sender.decorator;

import com.dianping.cat.report.alert.AlertType;
import com.dianping.cat.report.alert.sender.AlertEntity;

public class HeartbeatDecorator extends Decorator {

	public static final String ID = AlertType.HeartBeat.getName();

	@Override
	public String generateContent(AlertEntity alert) {
		return alert.getContent();
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[心跳告警] [项目: ").append(alert.getGroup()).append("][ip: ").append(alert.getParas().get("ip"))
		      .append("][指标: ").append(alert.getMetric()).append("]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}

}
