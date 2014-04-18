package com.dianping.cat.report.chart;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

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

		MetricAggregationGroup metricAggregationGroup = m_metricAggregationConfigManager.getMetricAggregationConfig()
		      .findMetricAggregationGroup(m_productLine);
		List<MetricAggregation> metricAggregations = metricAggregationGroup.getMetricAggregations();
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		Pair<String, LineChart> chart = null;

		for (MetricAggregation metricAggregation : metricAggregations) {
			chart = buildAggerationChart(datas, startDate, endDate, dataWithOutFutures, metricAggregation);
			charts.put(chart.getKey(), chart.getValue());
		}
		return charts;
	}

	public void rebuildData(Map<Long, Double> data, String operation) {

		String op = null;
		
		for (Entry<Long, Double> entry : data.entrySet()) {
			op = operation.replace("{data}", Double.toString(entry.getValue()));
			entry.setValue(new Operation(op).getResult());
		}
	}

	public <T> T getAttribute(T parentAttr, T myAttr) {
		return (myAttr == null ? parentAttr : myAttr);
	}

	private Pair<String, LineChart> buildAggerationChart(final Map<String, double[]> datas, Date startDate, Date endDate,
 final Map<String, double[]> dataWithOutFutures, MetricAggregation metricAggregation) {

		MetricAggregationGroup metricAggregationGroup = m_metricAggregationConfigManager.getMetricAggregationConfig()
		      .findMetricAggregationGroup(m_productLine);
		String type = metricAggregationGroup.getType();
		int step = m_dataExtractor.getStep();
		String id = metricAggregation.getId();
		String title = getAttribute(id, metricAggregation.getTitle());
		
		LineChart lineChart = new LineChart();
		lineChart.setStart(startDate);
		lineChart.setId(id);
		lineChart.setTitle(title);
		lineChart.setHtmlTitle(title);
		lineChart.setStep(step * TimeUtil.ONE_MINUTE);

		for (MetricAggregationItem metricAggregationItem : metricAggregation.getMetricAggregationItems()) {
			String domain = getAttribute(metricAggregation.getDomain(), metricAggregationItem.getDomain());
			String displayType = getAttribute(metricAggregation.getDisplayType(), metricAggregationItem.getDisplayType());
			boolean baseLine = getAttribute(metricAggregation.getBaseLine(), metricAggregationItem.getBaseLine());
			String operation = getAttribute(metricAggregation.getOperation(), metricAggregationItem.getOperation());
			String itemKey = domain + ":" + type + ":" + metricAggregationItem.getKey() + ":" + displayType.toUpperCase();

			if (dataWithOutFutures.containsKey(itemKey)) {
				Map<Long, Double> all = convertToMap(datas.get(itemKey), startDate, 1);
				Map<Long, Double> current = convertToMap(dataWithOutFutures.get(itemKey), startDate, step);
				addLastMinuteData(current, all, m_lastMinute, endDate);

				if (operation != null) {
					rebuildData(current, operation);
				}
				String suffix = null;
				if (MetricType.AVG.name().equalsIgnoreCase(displayType)) {
					suffix = Chinese.Suffix_AVG;
				} else if (MetricType.SUM.name().equalsIgnoreCase(displayType)) {
					suffix = Chinese.Suffix_SUM;
				} else if (MetricType.COUNT.name().equalsIgnoreCase(displayType)) {
					suffix = Chinese.Suffix_COUNT;
				}
				String key = metricAggregationItem.getKey() + suffix + Chinese.CURRENT_VALUE;
				lineChart.add(key, current);

				if (baseLine) {
					double[] baselines = queryBaseline(itemKey, startDate, endDate);
					Map<Long, Double> baselinesData = convertToMap(m_dataExtractor.extract(baselines), startDate, step);
					if (operation != null) {
						rebuildData(baselinesData, operation);
					}
					lineChart.add(metricAggregationItem.getKey() + suffix + Chinese.BASELINE_VALUE, baselinesData);
				}
			} else {
				m_logger.error("Error config in metricAggregationItem, item:" + metricAggregationItem);
			}
		}
		Pair<String, LineChart> chart = new Pair<String, LineChart>(id, lineChart);
		return chart;
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
			return new LinkedHashMap<String, LineChart>();
		}
	}

	@Override
	protected Map<String, double[]> buildGraphData(String productLine, MetricReport metricReport) {
		Map<String, double[]> currentValues = m_pruductDataFetcher.buildAllData(metricReport);
		
		return currentValues;
	}
}
