package com.dianping.cat.report.task.alert.network;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.task.alert.BaseAlertConfig;

public class NetworkAlertConfig extends BaseAlertConfig {

	private String m_id = "network";

	public String buildMailTitle(ProductLine productLine, String configTitle) {
		StringBuilder sb = new StringBuilder();

		sb.append("[网络告警] [产品线 ").append(productLine.getTitle()).append("]");
		sb.append("[网络指标 ").append(configTitle).append("]");
		return sb.toString();
	}

	public String getId() {
		return m_id;
	}

}
