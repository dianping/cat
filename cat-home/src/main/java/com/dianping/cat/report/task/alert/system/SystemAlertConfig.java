package com.dianping.cat.report.task.alert.system;

import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.report.task.alert.BaseAlertConfig;

public class SystemAlertConfig extends BaseAlertConfig {

	private String m_id = "system";

	public String buildMailTitle(ProductLine productLine, String configTitle) {
		StringBuilder sb = new StringBuilder();

		sb.append("[系统告警] [产品线 ").append(productLine.getTitle()).append("]");
		sb.append("[系统指标 ").append(configTitle).append("]");
		return sb.toString();
	}

	public String getId() {
		return m_id;
	}

}
