package com.dianping.cat.report.task.metric;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;

public class AlertConfig {

	public List<String> buildMailReceivers(ProductLine productLine) {
		List<String> emails = new ArrayList<String>();

		emails.add("yong.you@dianping.com");
		emails.add("he.huang@dianping.com");
		return emails;
	}

	public String buildMailTitle(ProductLine productLine, MetricItemConfig config) {
		String title = "业务告警, 产品线[" + productLine.getTitle() + "], 业务指标[" + config.getTitle() + "]";

		return title;
	}

	public String buildMailContent(ProductLine productLine, MetricItemConfig config) {
		String title = "业务告警, 产品线[" + productLine.getTitle() + "], 业务指标[" + config.getTitle() + "]";

		return title;
	}

	public boolean checkData(double[] value, double[] baseline) {
		int length = value.length;
		for (int i = 0; i < length; i++) {
			if (baseline[i] <= 0) {
				return false;
			}
			if (value[i] / baseline[i] >= 0.5 && (baseline[i] - value[i] > 10)) {
				return false;
			}
		}
		return true;
	}

}
