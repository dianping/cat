package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.sender.AlertEntity;

import freemarker.template.Configuration;

public class HeartbeatDecorator extends Decorator {

	public static final String ID = AlertType.HeartBeat.getName();

	public Configuration m_configuration;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[心跳告警] [指标: ").append(alert.getMetric()).append("]");
		return sb.toString();
	}

	@Override
	public String generateContent(AlertEntity alert) {
		return alert.getContent();
	}

}
