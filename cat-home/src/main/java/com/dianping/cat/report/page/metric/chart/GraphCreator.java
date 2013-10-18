package com.dianping.cat.report.page.metric.chart;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.advanced.MetricAnalyzer;
import com.dianping.cat.consumer.advanced.MetricConfigManager;
import com.dianping.cat.consumer.advanced.ProductLineConfigManager;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.baseline.BaselineService;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.task.metric.MetricType;

public class GraphCreator {

	@Inject
	private BaselineService m_baselineService;

	@Inject
	private DataExtractor m_dataExtractor;

	@Inject
	private MetricDataFetcher m_pruductDataFetcher;

	@Inject
	private CachedMetricReportService m_metricReportService;

	@Inject
	private MetricConfigManager m_configManager;

	@Inject
	private ProductLineConfigManager m_productLineConfigManager;

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Date startDate, Date endDate,
	      String abtestID) {
		long start = startDate.getTime();
		long end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeUtil.ONE_MINUTE);
		Map<String, double[]> allCurrentValues = new HashMap<String, double[]>();
		Map<String, double[]> allOneDayValues = new HashMap<String, double[]>();
		Map<String, double[]> allSevenDayValues = new HashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeUtil.ONE_HOUR) {
			List<String> domains = m_productLineConfigManager.queryProductLineDomains(productLine);
			List<MetricItemConfig> metricConfigs = m_configManager.queryMetricItemConfigs(new HashSet<String>(domains));
			MetricReport metricReport = m_metricReportService.query(productLine, new Date(start));
			MetricReport oneDayReport = m_metricReportService.query(productLine, new Date(start - TimeUtil.ONE_DAY));
			MetricReport sevenDayReport = m_metricReportService.query(productLine, new Date(start - TimeUtil.ONE_DAY * 7));
			Map<String, double[]> currentValues = m_pruductDataFetcher.buildGraphData(metricReport, metricConfigs,
			      abtestID);
			Map<String, double[]> oneDayValues = m_pruductDataFetcher
			      .buildGraphData(oneDayReport, metricConfigs, abtestID);
			Map<String, double[]> sevenDayValues = m_pruductDataFetcher.buildGraphData(sevenDayReport, metricConfigs,
			      abtestID);

			mergeMap(allCurrentValues, currentValues, totalSize, index);
			mergeMap(allOneDayValues, oneDayValues, totalSize, index);
			mergeMap(allSevenDayValues, sevenDayValues, totalSize, index);

			index++;
		}
		allCurrentValues = m_dataExtractor.extract(allCurrentValues);
		allOneDayValues = m_dataExtractor.extract(allOneDayValues);
		allSevenDayValues = m_dataExtractor.extract(allSevenDayValues);

		int step = m_dataExtractor.getStep();
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();

		for (Entry<String, double[]> entry : allCurrentValues.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			LineChart lineChart = new LineChart();

			lineChart.setTitle(findTitle(key));
			lineChart.setStart(startDate);
			lineChart.setSize(value.length);
			lineChart.setStep(step * TimeUtil.ONE_MINUTE);
			double[] baselines = queryBaseline(key, startDate, endDate);

			lineChart.add(Chinese.CURRENT_VALUE, allCurrentValues.get(key));
			lineChart.add(Chinese.BASELINE_VALUE, m_dataExtractor.extract(baselines));
			lineChart.add(Chinese.ONEDAY_VALUE, allOneDayValues.get(key));
			lineChart.add(Chinese.ONEWEEK_VALUE, allSevenDayValues.get(key));
			charts.put(key, lineChart);
		}
		return charts;
	}

	public Map<String, LineChart> buildDashboard(Date start, Date end, String abtestId) {
		Collection<ProductLine> productLines = m_productLineConfigManager.queryProductLines().values();
		Map<String, LineChart> allCharts = new LinkedHashMap<String, LineChart>();
		Map<String, LineChart> result = new LinkedHashMap<String, LineChart>();

		for (ProductLine productLine : productLines) {
			allCharts.putAll(buildChartsByProductLine(productLine.getId(), start, end, abtestId));
		}

		Collection<MetricItemConfig> configs = m_configManager.getMetricConfig().getMetricItemConfigs().values();

		for (MetricItemConfig config : configs) {
			String key = config.getId();
			if (config.getShowAvg() && config.getShowAvgDashboard()) {
				String avgKey = key + ":" + MetricType.AVG.name();
				put(allCharts, result, avgKey);
			}
			if (config.getShowCount() && config.getShowCountDashboard()) {
				String countKey = key + ":" + MetricType.COUNT.name();
				put(allCharts, result, countKey);
			}
			if (config.getShowSum() && config.getShowSumDashboard()) {
				String sumKey = key + ":" + MetricType.SUM.name();
				put(allCharts, result, sumKey);
			}
		}
		return result;
	}

	private String findTitle(String key) {
		int index = key.lastIndexOf(":");
		String id = key.substring(0, index);
		String type = key.substring(index + 1);
		MetricItemConfig config = m_configManager.queryMetricItemConfig(id);

		String des = "";
		if (MetricType.AVG.name().equals(type)) {
			des = Chinese.Suffix_AVG;
		} else if (MetricType.SUM.name().equals(type)) {
			des = Chinese.Suffix_SUM;
		} else if (MetricType.COUNT.name().equals(type)) {
			des = Chinese.Suffix_COUNT;
		}
		return config.getTitle() + des;
	}

	private void mergeMap(Map<String, double[]> all, Map<String, double[]> item, int size, int index) {
		for (Entry<String, double[]> entry : item.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			double[] result = all.get(key);

			if (result == null) {
				result = new double[size];
				all.put(key, result);
			}
			if (value != null) {
				int length = value.length;

				for (int i = 0; i < length; i++) {
					result[index * 60 + i] = value[i];
				}
			}
		}
	}

	private void put(Map<String, LineChart> charts, Map<String, LineChart> result, String key) {
		LineChart value = charts.get(key);

		if (value != null) {
			result.put(key, charts.get(key));
		}
	}

	private double[] queryBaseline(String key, Date start, Date end) {
		int size = (int) ((end.getTime() - start.getTime()) / TimeUtil.ONE_MINUTE);
		double[] result = new double[size];
		int index = 0;
		long startLong = start.getTime();
		long endLong = end.getTime();

		for (; startLong < endLong; startLong += TimeUtil.ONE_HOUR) {
			double[] values = m_baselineService.queryHourlyBaseline(MetricAnalyzer.ID, key, new Date(startLong));

			for (int j = 0; j < values.length; j++) {
				result[index * 60 + j] = values[j];
			}
			index++;
		}
		return result;
	}

}
