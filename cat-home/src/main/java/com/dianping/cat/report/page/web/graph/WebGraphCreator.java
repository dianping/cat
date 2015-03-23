package com.dianping.cat.report.page.web.graph;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.tuple.Pair;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.metric.model.entity.MetricItem;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Segment;
import com.dianping.cat.consumer.metric.model.entity.Statistic;
import com.dianping.cat.consumer.metric.model.entity.StatisticsItem;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;
import com.dianping.cat.report.graph.PieChart.Item;
import com.dianping.cat.report.graph.metric.AbstractGraphCreator;
import com.dianping.cat.report.page.metric.service.MetricReportMerger;
import com.dianping.cat.report.page.web.Handler.QueryEntity;

public class WebGraphCreator extends AbstractGraphCreator {

	private static final String COUNT = "访问量(次数/5分钟)";

	private static final String AVG = "平均响应时间(毫秒/5分钟)";

	public static final String SUCESS_PERCENT = "访问成功率(%/5分钟)";

	private List<PieChart> buildDetailPieChart(MetricReport report, String title) {
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
			chart.setTitle(entry.getKey() + "【" + title + "】");
			chart.addItems(items);
			charts.add(chart);
		}
		return charts;
	}

	public Pair<LineChart, PieChart> buildCodeChartData(final Map<String, Double[]> dataWithOutFutures, String type) {
		LineChart lineChart = new LineChart();
		lineChart.setUnit("");
		lineChart.setHtmlTitle(type + "平均分布(个/5分钟)");

		PieChart pieChart = new PieChart();
		List<PieChart.Item> items = new ArrayList<PieChart.Item>();

		pieChart.addItems(items);
		for (Entry<String, Double[]> entry : dataWithOutFutures.entrySet()) {
			String key = entry.getKey();
			Double[] value = entry.getValue();

			lineChart.add(key, value);
			double sum = computeSum(value);
			items.add(new Item().setTitle(entry.getKey()).setNumber(sum));
		}
		return new Pair<LineChart, PieChart>(lineChart, pieChart);
	}

	private Map<String, LineChart> buildInfoChartData(final Map<String, Double[]> dataWithOutFutures, Date startDate,
	      Date endDate, String title) {
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();

		for (Entry<String, Double[]> entry : dataWithOutFutures.entrySet()) {
			String key = entry.getKey();
			Double[] value = entry.getValue();
			LineChart lineChart = new LineChart();

			lineChart.setId(key);
			lineChart.setUnit("");
			lineChart.setTitle(key);
			lineChart.setHtmlTitle(key);
			lineChart.add(title, value);
			charts.put(key, lineChart);
		}
		return charts;
	}

	public double computeSum(Double[] data) {
		double result = 0;

		for (int i = 0; i < data.length; i++) {
			if (data[i] != null) {
				result = result + data[i];
			}
		}
		return result;
	}

	private Map<String, Double[]> fetchMetricCodeInfo(MetricReport report) {
		Map<String, MetricItem> items = report.getMetricItems();
		Map<String, Double[]> datas = new LinkedHashMap<String, Double[]>();

		for (Entry<String, MetricItem> item : items.entrySet()) {
			String id = item.getKey();
			int index = id.indexOf("|");
			String key = id.substring(index + 1);
			Map<Integer, Segment> segments = item.getValue().getSegments();
			Double[] data = datas.get(key);

			if (data == null) {
				data = new Double[12];
				datas.put(key, data);
			}
			for (Segment segment : segments.values()) {
				dataInc(data, segment.getId() / 5, segment.getCount());
			}
		}
		return datas;
	}

	private void dataInc(Double[] datas, int index, double value) {
		if (datas[index] != null) {
			datas[index] += value;
		} else {
			datas[index] = value;
		}
	}

	private Map<String, Double[]> fetchMetricInfoData(MetricReport report) {
		Map<String, Double[]> data = new LinkedHashMap<String, Double[]>();

		Double[] count = new Double[12];
		Double[] avg = new Double[12];
		Double[] avgCount = new Double[12];
		Double[] avgSum = new Double[12];
		Double[] error = new Double[12];
		Double[] successPercent = new Double[12];

		for (int i = 0; i < successPercent.length; i++) {
			successPercent[i] = 100.0;
		}

		data.put(COUNT, count);
		data.put(AVG, avg);
		data.put(SUCESS_PERCENT, successPercent);

		Map<String, MetricItem> items = report.getMetricItems();

		for (Entry<String, MetricItem> item : items.entrySet()) {
			String key = item.getKey();
			Map<Integer, Segment> segments = item.getValue().getSegments();

			for (Segment segment : segments.values()) {
				int id = segment.getId() / 5;

				if (key.endsWith(Constants.HIT)) {
					dataInc(count, id, segment.getCount());
				} else if (key.endsWith(Constants.ERROR)) {
					dataInc(error, id, segment.getCount());
				} else if (key.endsWith(Constants.AVG)) {
					dataInc(avgCount, id, segment.getCount());
					dataInc(avgSum, id, segment.getSum());
				}
			}
		}

		for (int i = 0; i < 12; i++) {
			if (avgSum[i] != null && avgCount[i] != null) {
				avg[i] = avgSum[i] / avgCount[i];
			}
		}

		for (int i = 0; i < 12; i++) {
			if (count[i] != null) {
				double sum = count[i];
				double success = count[i] - (error[i] != null ? error[i] : 0.0);

				if (sum > 0) {
					successPercent[i] = success / sum * 100.0;
				} else {
					successPercent[i] = 100.0;
				}
			}
		}
		return data;
	}

	protected void mergeValue(Map<String, Double[]> all, Map<String, Double[]> item, int size, int index) {
		for (Entry<String, Double[]> entry : item.entrySet()) {
			String key = entry.getKey();
			Double[] value = entry.getValue();
			Double[] result = all.get(key);

			if (result == null) {
				result = new Double[size];
				all.put(key, result);
				if (SUCESS_PERCENT.equals(key)) {
					for (int i = 0; i < size; i++) {
						result[i] = 100.0;
					}
				}
			}
			if (value != null) {
				int length = value.length;
				int pos = index;
				for (int i = 0; i < length && pos < size; i++, pos++) {
					result[pos] = value[i];
				}
			}
		}
	}

	private Map<String, Double[]> prepareAllData(MetricReport all, QueryEntity query) {
		long start = query.getStart().getTime(), end = query.getEnd().getTime();
		Date currentDay = TimeHelper.getCurrentDay(start);
		int totalSize = queryDuration(currentDay, 288);
		Map<String, Double[]> sourceValue = new LinkedHashMap<String, Double[]>();
		String type = query.getPars().get("type");
		MetricReportMerger merger = new MetricReportMerger(all);

		for (; start < end; start += TimeHelper.ONE_HOUR) {
			MetricReport report = m_metricReportService.queryUserMonitorReport(query.getUrl(), query.getPars(), new Date(
			      start));
			int index = (int) ((start - currentDay.getTime()) / (TimeHelper.ONE_MINUTE * 5));

			if (Constants.TYPE_INFO.equals(type)) {
				Map<String, Double[]> currentValues = fetchMetricInfoData(report);

				mergeValue(sourceValue, currentValues, totalSize, index);
				report.accept(merger);
			} else {
				Map<String, Double[]> currentValues = fetchMetricCodeInfo(report);

				mergeValue(sourceValue, currentValues, totalSize, index);
			}
		}
		return sourceValue;
	}

	public Pair<Map<String, LineChart>, List<PieChart>> queryBaseInfo(QueryEntity queryEntity, String title) {
		MetricReport report = new MetricReport(queryEntity.getUrl());
		Map<String, Double[]> oldCurrentValues = prepareAllData(report, queryEntity);
		Map<String, LineChart> lineCharts = buildInfoChartData(oldCurrentValues, queryEntity.getStart(),
		      queryEntity.getEnd(), title);
		List<PieChart> pieCharts = buildDetailPieChart(report, title);

		return new Pair<Map<String, LineChart>, List<PieChart>>(lineCharts, pieCharts);
	}

	private int queryDuration(Date period, int defaultValue) {
		Calendar cal = Calendar.getInstance();

		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);

		if (cal.getTime().equals(period)) {
			long start = cal.getTimeInMillis();
			long current = System.currentTimeMillis();
			int length = (int) (current - current % 300000 - start) / 300000;

			return length < 0 ? 0 : length;
		}
		return defaultValue;
	}

	public Pair<LineChart, PieChart> queryErrorInfo(QueryEntity queryEntity) {
		MetricReport report = new MetricReport(queryEntity.getUrl());
		Map<String, Double[]> oldCurrentValues = prepareAllData(report, queryEntity);

		return buildCodeChartData(oldCurrentValues, queryEntity.getType());
	}
}
