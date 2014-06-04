package com.dianping.cat.report.task.metric;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.tuple.Pair;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;

public class MetricAlertConfig extends BaseAlertConfig {

	private String m_ID = "metric";
	
	public Pair<Boolean, String> checkData(MetricItemConfig config, double[] value, double[] baseline, MetricType type) {
		int length = value.length;
		StringBuilder baselines = new StringBuilder();
		StringBuilder values = new StringBuilder();
		double decreasePercent = config.getDecreasePercentage();
		double decreaseValue = config.getDecreaseValue();
		double valueSum = 0;
		double baselineSum = 0;
		DecimalFormat df = new DecimalFormat("0.0");

		if (decreasePercent == 0) {
			decreasePercent = 50;
		}
		if (decreaseValue == 0) {
			decreaseValue = 100;
		}

		for (int i = 0; i < length; i++) {
			baselines.append(df.format(baseline[i])).append(" ");
			values.append(df.format(value[i])).append(" ");
			valueSum = valueSum + value[i];
			baselineSum = baselineSum + baseline[i];

			if (baseline[i] <= 0) {
				baseline[i] = 100;
				return new Pair<Boolean, String>(false, "");
			}
			if (type == MetricType.COUNT || type == MetricType.SUM) {
				if (value[i] / baseline[i] > (1 - decreasePercent / 100) || (baseline[i] - value[i]) < decreaseValue) {
					return new Pair<Boolean, String>(false, "");
				}
			}
		}
		double percent = (1 - valueSum / baselineSum) * 100;
		StringBuilder sb = new StringBuilder();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		sb.append("[基线值:").append(baselines.toString()).append("] ");
		sb.append("[实际值:").append(values.toString()).append("] ");
		sb.append("[下降:").append(df.format(percent)).append("%").append("]");
		sb.append("[告警时间:").append(sdf.format(new Date()) + "]");
		return new Pair<Boolean, String>(true, sb.toString());
	}
	
	public String getID() {
		return m_ID;
	}

}
