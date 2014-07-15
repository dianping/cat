package com.dianping.cat.report.task.alert.network;

import com.dianping.cat.report.task.alert.BaseAlertConfig;

public class NetworkAlertConfig extends BaseAlertConfig {

	private String m_id = "network";

	@Override
	public String buildMailTitle(String productlineName, String configTitle) {
		StringBuilder sb = new StringBuilder();

		sb.append("[网络告警] [产品线 ").append(productlineName).append("]");
		sb.append("[网络指标 ").append(configTitle).append("]");
		return sb.toString();
	}

	public String getId() {
		return m_id;
	}

}
