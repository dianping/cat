package com.dianping.cat.report.task.alert;

import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.consumer.metric.MetricAnalyzer;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.report.service.BaselineService;
import com.dianping.cat.service.ModelPeriod;

public class DefaultDataExtractor implements DataExtractor {

	@Inject
	protected BaselineService m_baselineService;

	@Override
	public Pair<double[], double[]> extractData(int currentMinute, int ruleMinute, MetricReport lastReport,
	      MetricReport currentReport, String metricKey, MetricType type) {
		Pair<double[], double[]> result = new Pair<double[], double[]>();
		double[] value = new double[ruleMinute];
		double[] baseline = new double[ruleMinute];

		if (currentMinute >= ruleMinute - 1) {
			if (currentReport != null) {
				int start = currentMinute + 1 - ruleMinute;
				int end = currentMinute;

				value = queryRealData(start, end, metricKey, currentReport, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.CURRENT.getStartTime()), type);
			}
		} else if (currentMinute < 0) {
			if (lastReport != null) {
				int start = 60 + currentMinute + 1 - (ruleMinute);
				int end = 60 + currentMinute;

				value = queryRealData(start, end, metricKey, lastReport, type);
				baseline = queryBaseLine(start, end, metricKey, new Date(ModelPeriod.LAST.getStartTime()), type);
			}
		} else {
			if (currentReport != null && lastReport != null) {
				int currentStart = 0, currentEnd = currentMinute;
				double[] currentValue = queryRealData(currentStart, currentEnd, metricKey, currentReport, type);
				double[] currentBaseline = queryBaseLine(currentStart, currentEnd, metricKey,
				      new Date(ModelPeriod.CURRENT.getStartTime()), type);

				int lastStart = 60 + 1 - (ruleMinute - currentMinute);
				int lastEnd = 59;
				double[] lastValue = queryRealData(lastStart, lastEnd, metricKey, lastReport, type);
				double[] lastBaseline = queryBaseLine(lastStart, lastEnd, metricKey,
				      new Date(ModelPeriod.LAST.getStartTime()), type);

				value = mergerArray(lastValue, currentValue);
				baseline = mergerArray(lastBaseline, currentBaseline);
			}
		}

		result.setKey(baseline);
		result.setValue(value);
		return result;
	}

	@Override
	public double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		for (int i = 0; i < toLength; i++) {
			result[i + index] = to[i];
		}
		return result;
	}

	private double[] queryBaseLine(int start, int end, String baseLineKey, Date date, MetricType type) {
		double[] baseline = m_baselineService.queryHourlyBaseline(MetricAnalyzer.ID, baseLineKey + ":" + type, date);
		int length = end - start + 1;
		double[] result = new double[length];

		if (baseline != null) {
			System.arraycopy(baseline, start, result, 0, length);
		}

		return result;
	}

	private double[] queryRealData(int start, int end, String metricKey, MetricReport report, MetricType type) {
		if (report == null) {
		}

		double[] all = new double[60];
		Map<Integer, Segment> map = report.findOrCreateMetricItem(metricKey).getSegments();

		for (Entry<Integer, Segment> entry : map.entrySet()) {
			Integer minute = entry.getKey();
			Segment seg = entry.getValue();

			if (type == MetricType.AVG) {
				all[minute] = seg.getAvg();
			} else if (type == MetricType.COUNT) {
				all[minute] = (double) seg.getCount();
			} else if (type == MetricType.SUM) {
				all[minute] = seg.getSum();
			}
		}
		int length = end - start + 1;
		double[] result = new double[length];
		System.arraycopy(all, start, result, 0, length);

		return result;
	}
}
