package com.dianping.cat.report.chart;

import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregation;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationGroup;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationItem;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.task.metric.MetricType;
import com.dianping.cat.system.config.MetricAggregationConfigManager;

public class AggregationGraphCreator extends GraphCreatorBase {

	@Inject
	private MetricAggregationConfigManager m_metricAggregationConfigManager;

	private String m_productLine;

	private Map<String, LineChart> buildChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures) {

		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		buildChartDataForAggregation(datas, startDate, endDate, dataWithOutFutures, charts);
		appendNoAggregationCharts(datas, startDate, endDate, dataWithOutFutures, charts);
		return charts;
	}

	public void operateData(Map<Long, Double> data, String operation) {
		int index = operation.indexOf("{data}");
		String prefix = operation.substring(0, index);
		String suffix = operation.substring(index + "{data}".length());
		for (Entry<Long, Double> entry : data.entrySet()) {
			String op = prefix + entry.getValue() + suffix;
			entry.setValue(new Operation(op).getResult());
		}
	}

	public <T> T getAttribute(T parentAttr, T myAttr) {
		return (myAttr == null ? parentAttr : myAttr);
	}

	public Map<String, LineChart> buildChartDataForAggregation(final Map<String, double[]> datas, Date startDate,
	      Date endDate, final Map<String, double[]> dataWithOutFutures, Map<String, LineChart> charts) {

		MetricAggregationGroup metricAggregationGroup = m_metricAggregationConfigManager.getMetricAggregationConfig()
		      .findMetricAggregationGroup(m_productLine);
		List<MetricAggregation> metricAggregations = metricAggregationGroup.getMetricAggregations();
		String type = metricAggregationGroup.getType();
		int step = m_dataExtractor.getStep();

		if (dataWithOutFutures.size() == 0)
			return charts;

		for (MetricAggregation metricAggregation : metricAggregations) {
			String title = metricAggregation.getId();
			LineChart lineChart = new LineChart();
			lineChart.setStart(startDate);
			lineChart.setId(title);
			lineChart.setTitle(title);
			lineChart.setHtmlTitle(title);
			lineChart.setStep(step * TimeUtil.ONE_MINUTE);
			int size = 0;

			for (MetricAggregationItem metricAggregationItem : metricAggregation.getMetricAggregationItems()) {
				String domain = getAttribute(metricAggregation.getDomain(), metricAggregationItem.getDomain());
				String displayType = getAttribute(metricAggregation.getDisplayType(),
				      metricAggregationItem.getDisplayType());
				boolean baseLine = getAttribute(metricAggregation.getBaseLine(), metricAggregationItem.getBaseLine());
				String operation = getAttribute(metricAggregation.getOperation(), metricAggregationItem.getOperation());
				
				String itemKey = domain + ":" + type + ":" + metricAggregationItem.getKey() + ":"
				      + displayType.toUpperCase();
				size += dataWithOutFutures.get(itemKey).length;
				Map<Long, Double> all = convertToMap(datas.get(itemKey), startDate, 1);
				Map<Long, Double> current = convertToMap(dataWithOutFutures.get(itemKey), startDate, step);
				addLastMinuteData(current, all, m_lastMinute, endDate);
				if (operation != null) {
					operateData(current, operation);
				}
				String suffix = null;
				if (MetricType.AVG.name().equals(displayType.toUpperCase())) {
					suffix = Chinese.Suffix_AVG;
				} else if (MetricType.SUM.name().equals(displayType.toUpperCase())) {
					suffix = Chinese.Suffix_SUM;
				} else if (MetricType.COUNT.name().equals(displayType.toUpperCase())) {
					suffix = Chinese.Suffix_COUNT;
				}
				lineChart.add(metricAggregationItem.getKey() + suffix + Chinese.CURRENT_VALUE, current);
				if (baseLine) {
					double[] baselines = queryBaseline(itemKey, startDate, endDate);
					Map<Long, Double> baselinesData = convertToMap(m_dataExtractor.extract(baselines), startDate, step);
					if (operation != null) {
						operateData(baselinesData, operation);
					}
					lineChart.add(metricAggregationItem.getKey() + suffix + Chinese.BASELINE_VALUE, baselinesData);
				}
			}
			lineChart.setSize(size);
			charts.put(title, lineChart);
		}
		return charts;
	}

	public void appendNoAggregationCharts(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures, Map<String, LineChart> charts) {

		if (dataWithOutFutures.size() == 0)
			return;

		List<MetricItemConfig> alertItems = m_alertInfo.getLastestAlarm(5);
		int step = m_dataExtractor.getStep();

		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(m_productLine);
		List<MetricItemConfig> metricConfigs = m_metricConfigManager.queryMetricItemConfigs(new HashSet<String>(domains));

		for (MetricItemConfig config : metricConfigs) {
			String key = config.getId();
			if (!config.getShowAvg()) {
				String avgKey = key + ":" + MetricType.AVG.name();
				dataWithOutFutures.remove(avgKey);
			}
			if (!config.getShowCount()) {
				String countKey = key + ":" + MetricType.COUNT.name();
				dataWithOutFutures.remove(countKey);
			}
			if (!config.getShowSum()) {
				String sumKey = key + ":" + MetricType.SUM.name();
				dataWithOutFutures.remove(sumKey);
			}
		}

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
	}

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Date startDate, Date endDate) {
		m_productLine = productLine;
		MetricAggregationGroup metricAggregationGroup = m_metricAggregationConfigManager.getMetricAggregationConfig()
		      .findMetricAggregationGroup(m_productLine);

		if (metricAggregationGroup != null) {
			Map<String, double[]> oldCurrentValues = prepareAllData(m_productLine, startDate, endDate);
			Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
			Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);
			return buildChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures);
		} else {
			return null;
		}
	}

	@Override
	protected Map<String, double[]> buildGraphData(String productLine, MetricReport metricReport) {
		Map<String, double[]> currentValues = m_pruductDataFetcher.buildGraphData(metricReport, null);
		return currentValues;
	}
}
