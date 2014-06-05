package com.dianping.cat.report.task.alert.metric;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.task.alert.BaseAlertConfig;

public class MetricAlertConfig extends BaseAlertConfig {

	private String m_id = "metric";

	public String buildMailTitle(ProductLine productLine, String configTitle) {
		StringBuilder sb = new StringBuilder();

		sb.append("[业务告警] [产品线 ").append(productLine.getTitle()).append("]");
		sb.append("[业务指标 ").append(configTitle).append("]");
		return sb.toString();
	}

	public String getId() {
		return m_id;
	}

}
