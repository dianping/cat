package com.dianping.cat.report.task.metric;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;

public class NetworkAlertConfig extends BaseAlertConfig {

	private String m_id = "network";

	public String buildMailTitle(ProductLine productLine, MetricItemConfig config) {
		StringBuilder sb = new StringBuilder();

		sb.append("[网络告警] [产品线 ").append(productLine.getTitle()).append("]");
		sb.append("[网络指标 ").append(config.getTitle()).append("]");
		return sb.toString();
	}

	public String getId() {
		return m_id;
	}

}
