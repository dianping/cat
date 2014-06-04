package com.dianping.cat.report.chart;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.metricGroup.entity.MetricKeyConfig;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.task.metric.MetricType;

public class MetricGraphCreator extends AbstractGraphCreator {

	public Map<String, LineChart> buildChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures) {
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		List<MetricItemConfig> alertItems = m_alertInfo.queryLastestAlarmInfo(5);
		int step = m_dataExtractor.getStep();

		for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			LineChart lineChart = new LineChart();

			buildLineChartTitle(alertItems, lineChart, key);
			lineChart.setStart(startDate);
			lineChart.setSize(value.length);
			lineChart.setStep(step * TimeUtil.ONE_MINUTE);
			double[] baselines = queryBaseline(key, startDate, endDate);

			Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
			Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

			addLastMinuteData(current, all, m_lastMinute, endDate);
			lineChart.add(Chinese.CURRENT_VALUE, current);
			lineChart.add(Chinese.BASELINE_VALUE, convertToMap(m_dataExtractor.extract(baselines), startDate, step));
			charts.put(key, lineChart);
		}
		return charts;
	}

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Date startDate, Date endDate) {
		Map<String, double[]> oldCurrentValues = prepareAllData(productLine, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);
		return buildChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures);
	}

	public Map<String, LineChart> buildDashboard(Date start, Date end) {
		Collection<ProductLine> productLines = m_productLineConfigManager.queryAllProductLines().values();
		Map<String, LineChart> allCharts = new LinkedHashMap<String, LineChart>();

		for (ProductLine productLine : productLines) {
			if (showInDashboard(productLine.getId())) {
				allCharts.putAll(buildChartsByProductLine(productLine.getId(), start, end));
			}
		}
		List<MetricItemConfig> configs = new ArrayList<MetricItemConfig>(m_metricConfigManager.getMetricConfig()
		      .getMetricItemConfigs().values());

		Collections.sort(configs, new Comparator<MetricItemConfig>() {
			@Override
			public int compare(MetricItemConfig o1, MetricItemConfig o2) {
				return (int) (o1.getShowDashboardOrder() * 100 - o2.getShowDashboardOrder() * 100);
			}
		});

		Map<String, LineChart> result = new LinkedHashMap<String, LineChart>();
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

	protected boolean isProductLineInGroup(String productLine, List<MetricKeyConfig> configs) {
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine);
		List<MetricItemConfig> metricConfig = m_metricConfigManager.queryMetricItemConfigs(domains);

		for (MetricKeyConfig metric : configs) {
			String domain = metric.getMetricDomain();
			String type = metric.getMetricType();
			String key = metric.getMetricKey();

			for (MetricItemConfig item : metricConfig) {
				if (item.getDomain().equalsIgnoreCase(domain) && item.getType().equalsIgnoreCase(type)
				      && item.getMetricKey().equalsIgnoreCase(key)) {
					return true;
				}
			}
		}
		return false;
	}

	public Map<String, LineChart> buildDashboardByGroup(Date start, Date end, String metricGroup) {
		Map<String, LineChart> result = new LinkedHashMap<String, LineChart>();
		List<MetricKeyConfig> metricConfigs = m_metricGroupConfigManager.queryMetricGroupConfig(metricGroup);
		Collection<ProductLine> productLines = m_productLineConfigManager.queryAllProductLines().values();
		Map<String, LineChart> allCharts = new LinkedHashMap<String, LineChart>();

		for (ProductLine productLine : productLines) {
			if (isProductLineInGroup(productLine.getId(), metricConfigs)) {
				allCharts.putAll(buildChartsByProductLine(productLine.getId(), start, end));
			}
		}
		for (MetricKeyConfig metric : metricConfigs) {
			String domain = metric.getMetricDomain();
			String type = metric.getMetricType().equalsIgnoreCase("metric") ? "Metric" : metric.getMetricType();
			String key = metric.getMetricKey();
			String id = m_metricConfigManager.buildMetricKey(domain, type, key) + ":"
			      + metric.getDisplayType().toUpperCase();
			put(allCharts, result, id);
		}
		return result;
	}

	private boolean showInDashboard(String productline) {
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productline);

		List<MetricItemConfig> configs = m_metricConfigManager.queryMetricItemConfigs(domains);
		for (MetricItemConfig config : configs) {
			if (config.isShowAvgDashboard() || config.isShowCountDashboard() || config.isShowSumDashboard()) {
				return true;
			}
		}
		return false;
	}

	protected String queryMetricItemDes(String type) {
		String des = "";

		if (MetricType.AVG.name().equals(type)) {
			des = Chinese.Suffix_AVG;
		} else if (MetricType.SUM.name().equals(type)) {
			des = Chinese.Suffix_SUM;
		} else if (MetricType.COUNT.name().equals(type)) {
			des = Chinese.Suffix_COUNT;
		}
		return des;
	}

	protected void buildLineChartTitle(List<MetricItemConfig> alertItems, LineChart chart, String key) {
		int index = key.lastIndexOf(":");
		String metricId = key.substring(0, index);
		String type = key.substring(index + 1);
		MetricItemConfig config = m_metricConfigManager.queryMetricItemConfig(metricId);
		if (config != null) {
			String des = queryMetricItemDes(type);
			String title = config.getTitle() + des;

			chart.setTitle(title);
			chart.setId(metricId + ":" + type);

			if (alertItems.contains(config)) {
				chart.setHtmlTitle("<span style='color:red'>" + title + "</span>");
			} else {
				chart.setHtmlTitle(title);
			}
		}
	}

	protected Map<String, double[]> buildGraphData(MetricReport metricReport, List<MetricItemConfig> metricConfigs) {
		Map<String, double[]> datas = m_pruductDataFetcher.buildGraphData(metricReport, metricConfigs);
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();

		for (MetricItemConfig config : metricConfigs) {
			String key = config.getId();

			if (config.getShowAvg()) {
				String avgKey = key + ":" + MetricType.AVG.name();
				putKey(datas, values, avgKey);
			}
			if (config.getShowCount()) {
				String countKey = key + ":" + MetricType.COUNT.name();
				putKey(datas, values, countKey);
			}
			if (config.getShowSum()) {
				String sumKey = key + ":" + MetricType.SUM.name();
				putKey(datas, values, sumKey);
			}
		}
		return values;
	}

}
