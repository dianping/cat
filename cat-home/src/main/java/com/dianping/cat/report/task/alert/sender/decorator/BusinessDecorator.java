package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.sender.AlertConstants;
import com.dianping.cat.report.task.alert.sender.AlertEntity;

public class BusinessDecorator extends DefaultDecorator {

	public static final String ID = AlertConstants.BUSINESS;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[业务告警] [产品线 ").append(alert.getGroup()).append("]");
		sb.append("[业务指标 ").append(alert.getMetric()).append("]");
		return sb.toString();
	}

}
