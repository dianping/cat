package com.dianping.cat.report.page.userMonitor.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.alibaba.cobar.parser.util.Pair;
import com.dianping.cat.Monitor;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.entity.Statistic;
import com.dianping.cat.consumer.metric.model.entity.StatisticsItem;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.BaseGraphCreator;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.PieChart;
import com.dianping.cat.report.page.PieChart.Item;
import com.dianping.cat.report.page.model.metric.MetricReportMerger;

public class DefaultUserMonitGraphCreator extends BaseGraphCreator implements UserMonitorGraphCreator {

	private static final String COUNT = "每分钟访问量(次数)";

	private static final String AVG = "每分钟响应时间(ms)";

	private static final String SUCESS_PERCENT = "每分钟调用成功率(%)";

	private List<PieChart> buildDetailPieChart(MetricReport report) {
		Map<String, Statistic> statics = report.getStatistics();
		List<PieChart> charts = new ArrayList<PieChart>();

		for (Entry<String, Statistic> entry : statics.entrySet()) {
			PieChart chart = new PieChart().setMaxSize(Integer.MAX_VALUE);
			List<Item> items = new ArrayList<Item>();
			Statistic values = entry.getValue();
			Map<String, StatisticsItem> statisticsItems = values.getStatisticsItems();

			for (StatisticsItem tmp : statisticsItems.values()) {
				Item item = new Item();

				item.setNumber(tmp.getCount());
				item.setTitle(tmp.getId());
				items.add(item);
			}
			chart.setTitle(entry.getKey());
			chart.addItems(items);
			charts.add(chart);
		}
		return charts;
	}

	public Pair<LineChart, PieChart> buildErrorChartData(final Map<String, double[]> datas, Date startDate,
	      Date endDate, final Map<String, double[]> dataWithOutFutures) {
		LineChart lineChart = new LineChart();
		int step = m_dataExtractor.getStep();
		lineChart.setStart(startDate);
		lineChart.setStep(step * TimeUtil.ONE_MINUTE);
		PieChart pieChart = new PieChart();
		List<PieChart.Item> items = new ArrayList<PieChart.Item>();

		pieChart.addItems(items);
		for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
			Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

			addLastMinuteData(current, all, m_lastMinute, endDate);
			lineChart.setSize(value.length);
			lineChart.add(entry.getKey(), current);

			double sum = computeSum(current);
			items.add(new Item().setTitle(entry.getKey()).setNumber(sum));
		}
		return new Pair<LineChart, PieChart>(lineChart, pieChart);
	}

	@Override
	protected Map<String, double[]> buildGraphData(MetricReport metricReport, List<MetricItemConfig> metricConfigs) {
		throw new RuntimeException("unsupport in user monitor graph!");
	}

	private Map<String, LineChart> buildInfoChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures) {
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();

		int step = m_dataExtractor.getStep();

		for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			LineChart lineChart = new LineChart();

			lineChart.setId(key);
			lineChart.setTitle(key);
			lineChart.setStart(startDate);
			lineChart.setSize(value.length);
			lineChart.setStep(step * TimeUtil.ONE_MINUTE);

			Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
			Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

			addLastMinuteData(current, all, m_lastMinute, endDate);
			lineChart.add(entry.getKey(), current);
			charts.put(key, lineChart);
		}
		return charts;
	}

	public double computeSum(Map<Long, Double> data) {
		double result = 0;

		for (double d : data.values()) {
			result = result + d;
		}
		return result;
	}

	private Map<String, double[]> fetchMetricCodeInfo(MetricReport report) {
		Map<String, MetricItem> items = report.getMetricItems();
		Map<String, double[]> datas = new LinkedHashMap<String, double[]>();

		for (Entry<String, MetricItem> item : items.entrySet()) {
			String id = item.getKey();
			int index = id.indexOf("|");
			String key = id.substring(index + 1);
			Map<Integer, Segment> segments = item.getValue().getSegments();
			double[] data = datas.get(key);

			if (data == null) {
				data = new double[60];
				datas.put(key, data);
			}
			for (Segment segment : segments.values()) {
				int count = segment.getCount();
				int minute = segment.getId();

				data[minute] = count;
			}
		}
		return datas;
	}

	private Map<String, double[]> fetchMetricInfoData(MetricReport report) {
		Map<String, double[]> data = new LinkedHashMap<String, double[]>();

		double[] count = new double[60];
		double[] avg = new double[60];
		double[] error = new double[60];
		double[] successPercent = new double[60];

		data.put(COUNT, count);
		data.put(AVG, avg);
		data.put(SUCESS_PERCENT, successPercent);

		Map<String, MetricItem> items = report.getMetricItems();

		for (Entry<String, MetricItem> item : items.entrySet()) {
			String key = item.getKey();
			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId();

				if (key.endsWith(Monitor.HIT)) {
					count[id] = segment.getCount();
				} else if (key.endsWith(Monitor.ERROR)) {
					error[id] = segment.getCount();
				} else if (key.endsWith(Monitor.AVG)) {
					avg[id] = segment.getAvg();
				}
			}
		}

		for (int i = 0; i < 60; i++) {
			double sum = count[i] + error[i];

			if (sum > 0) {
				successPercent[i] = count[i] / sum * 100.0;
			} else {
				successPercent[i] = 100;
			}
		}
		return data;
	}

	private Map<String, double[]> prepareAllData(MetricReport all, String url, Map<String, String> pars, Date startDate,
	      Date endDate) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeUtil.ONE_MINUTE);
		Map<String, double[]> sourceValue = new LinkedHashMap<String, double[]>();
		int index = 0;
		String type = pars.get("type");
		MetricReportMerger merger = new MetricReportMerger(all);

		for (; start < end; start += TimeUtil.ONE_HOUR) {
			MetricReport report = m_metricReportService.queryUserMonitorReport(url, pars, new Date(start));

			if (Monitor.TYPE_INFO.equals(type)) {
				Map<String, double[]> currentValues = fetchMetricInfoData(report);

				mergeMap(sourceValue, currentValues, totalSize, index);
				report.accept(merger);
			} else {
				Map<String, double[]> currentValues = fetchMetricCodeInfo(report);

				mergeMap(sourceValue, currentValues, totalSize, index);
			}
			index++;
		}
		return sourceValue;
	}

	@Override
	public Pair<Map<String, LineChart>, List<PieChart>> queryBaseInfo(Date startDate, Date endDate, String url,
	      Map<String, String> pars) {
		MetricReport report = new MetricReport(url);
		Map<String, double[]> oldCurrentValues = prepareAllData(report, url, pars, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);

		Map<String, LineChart> lineCharts = buildInfoChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures);
		List<PieChart> pieCharts = buildDetailPieChart(report);
		return new Pair<Map<String, LineChart>, List<PieChart>>(lineCharts, pieCharts);
	}

	@Override
	public Pair<LineChart, PieChart> queryErrorInfo(Date startDate, Date endDate, String url, Map<String, String> pars) {
		MetricReport report = new MetricReport(url);
		Map<String, double[]> oldCurrentValues = prepareAllData(report, url, pars, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);

		return buildErrorChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures);
	}

}
