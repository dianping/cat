package com.dianping.cat.report.task.metric;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.unidal.tuple.Pair;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;

public class AlertConfig {

	private DecimalFormat m_df = new DecimalFormat("0.0");

	public List<String> buildMailReceivers(ProductLine productLine) {
		List<String> emails = new ArrayList<String>();

		emails.add("yong.you@dianping.com");
		return emails;
	}

	public String buildMailTitle(ProductLine productLine, MetricItemConfig config) {
		String title = "[业务告警] 产品线[" + productLine.getTitle() + "] 业务指标[" + config.getTitle() + "]";

		return title;
	}

	public Pair<Boolean, String> checkData(double[] value, double[] baseline, MetricType type) {
		int length = value.length;
		StringBuilder baselines = new StringBuilder();
		StringBuilder values = new StringBuilder();

		for (int i = 0; i < length; i++) {
			baselines.append(m_df.format(baseline[i])).append(",");
			values.append(m_df.format(value[i])).append(",");

			if (baseline[i] <= 0) {
				return new Pair<Boolean, String>(false, "");
			}
			if (type == MetricType.COUNT || type == MetricType.SUM) {
				if (value[i] / baseline[i] > 0.5 || baseline[i] < 20) {
					return new Pair<Boolean, String>(false, "");
				}
			} else if (type == MetricType.AVG) {
				if (value[i] / baseline[i] < 2 || baseline[i] < 5) {
					return new Pair<Boolean, String>(false, "");
				}
			}
		}
		return new Pair<Boolean, String>(true, type + " baselines:" + baselines.toString() + " value:"
		      + values.toString());
	}

}
