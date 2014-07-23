package com.dianping.cat.report.task.alert.system;

import com.dianping.cat.report.task.alert.BaseAlertConfig;

public class SystemAlertConfig extends BaseAlertConfig {

	private String m_id = "system";

	@Override
	public String buildMailTitle(String productlineName, String metricName) {
		StringBuilder sb = new StringBuilder();

		sb.append("[系统告警] [产品线 ").append(productlineName).append("]");
		sb.append("[系统指标 ").append(metricName).append("]");
		return sb.toString();
	}

	public String getId() {
		return m_id;
	}

}
