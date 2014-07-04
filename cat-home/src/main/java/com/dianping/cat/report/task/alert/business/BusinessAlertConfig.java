package com.dianping.cat.report.task.alert.business;

import com.dianping.cat.report.task.alert.BaseAlertConfig;

public class BusinessAlertConfig extends BaseAlertConfig {

	private String m_id = "business";

	@Override
	public String buildMailTitle(String productlineName, String configTitle) {
		StringBuilder sb = new StringBuilder();

		sb.append("[业务告警] [产品线 ").append(productlineName).append("]");
		sb.append("[业务指标 ").append(configTitle).append("]");
		return sb.toString();
	}

	public String getId() {
		return m_id;
	}

}
