package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class NetworkDecorator extends ProductlineDecorator {

	public static final String ID = AlertType.Network.getName();

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[网络告警] [产品线 ").append(alert.getGroup()).append("]");
		sb.append("[网络指标 ").append(alert.getMetric()).append("]");
		return sb.toString();
	}

}
