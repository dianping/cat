package com.dianping.cat.report.task.alert;

import org.unidal.tuple.Pair;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;

public interface DataExtractor {

	public Pair<double[], double[]> extractData(int currentMinute, int ruleMinute, MetricReport lastReport,
	      MetricReport currentReport, String metricKey, MetricType type);

	public double[] mergerArray(double[] lastValue, double[] currentValue);
	
}
