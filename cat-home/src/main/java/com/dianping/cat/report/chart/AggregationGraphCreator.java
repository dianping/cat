package com.dianping.cat.report.chart;

import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregation;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationGroup;
import com.dianping.cat.home.metricAggregation.entity.MetricAggregationItem;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.task.metric.MetricType;
import com.dianping.cat.system.config.MetricAggregationConfigManager;

public class AggregationGraphCreator extends BaseGraphCreator {

	@Inject
	private MetricAggregationConfigManager m_metricAggregationConfigManager;

	private String m_productLine;
	
	private String m_aggregationGroup;

	private Map<String, LineChart> buildChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures) {
		MetricAggregationGroup metricAggregationGroup = m_metricAggregationConfigManager.getMetricAggregationConfig()
		      .findMetricAggregationGroup(m_aggregationGroup);
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

	private Pair<String, LineChart> buildAggerationChart(final Map<String, double[]> datas, Date startDate,
	      Date endDate, final Map<String, double[]> dataWithOutFutures, MetricAggregation metricAggregation) {
		MetricAggregationGroup metricAggregationGroup = m_metricAggregationConfigManager.getMetricAggregationConfig()
		      .findMetricAggregationGroup(m_aggregationGroup);
		List<MetricItemConfig> alertItems = m_alertInfo.queryLastestAlarmInfo(5);
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
				buildLineChartTitle(alertItems, lineChart, itemKey);
				
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

	protected boolean isProductLineInGroup(String productLine, List<MetricAggregation> metricAggregations) {
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine);
		List<MetricItemConfig> metricConfigs = m_metricConfigManager.queryMetricItemConfigs(domains);
		
		for(MetricItemConfig metricConfig : metricConfigs){
			String domain = metricConfig.getDomain();
			String type = metricConfig.getType();
			String key = metricConfig.getMetricKey();
			if(!type.equalsIgnoreCase("Metric")) {
				return false;
			}
			for (MetricAggregation metricAggregation : metricAggregations) {
				for (MetricAggregationItem item : metricAggregation.getMetricAggregationItems()) {
					String myDomain = getAttribute(metricAggregation.getDomain(), item.getDomain());
					String myKey = item.getKey();
					if (myDomain.equalsIgnoreCase(domain) && myKey.equalsIgnoreCase(key)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	public Map<String, LineChart> buildDashboardByGroup(Date start, Date end, String metricGroup) {
		m_aggregationGroup = metricGroup;
		
		Map<String, LineChart> result = new LinkedHashMap<String, LineChart>();
		MetricAggregationGroup metricAggregationGroup = m_metricAggregationConfigManager.getMetricAggregationConfig()
		      .findMetricAggregationGroup(metricGroup);
		Collection<ProductLine> productLines = m_productLineConfigManager.queryAllProductLines().values();
		Map<String, LineChart> allCharts = new LinkedHashMap<String, LineChart>();

		for (ProductLine productLine : productLines) {
			if (isProductLineInGroup(productLine.getId(), metricAggregationGroup.getMetricAggregations())) {
				result = buildChartsByProductLine(productLine.getId(), start, end);
				allCharts.putAll(result);
			}
		}
		return allCharts;
	}

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Date startDate, Date endDate) {
		m_productLine = productLine;
		MetricAggregationGroup metricAggregationGroup = m_metricAggregationConfigManager.getMetricAggregationConfig()
		      .findMetricAggregationGroup(m_aggregationGroup);

		if (metricAggregationGroup != null) {
			Map<String, double[]> oldCurrentValues = prepareAllData(m_productLine, startDate, endDate);
			Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
			Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);

			return buildChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures);
		} else {
			return new LinkedHashMap<String, LineChart>();
		}
	}

	protected Map<String, double[]> buildGraphData(MetricReport metricReport, List<MetricItemConfig> metricConfigs) {
		Map<String, double[]> datas = m_pruductDataFetcher.buildGraphData(metricReport, metricConfigs);
		Map<String, double[]> values = new LinkedHashMap<String, double[]>();

		for (MetricItemConfig config : metricConfigs) {
			String key = config.getId();

			String avgKey = key + ":" + MetricType.AVG.name();
			putKey(datas, values, avgKey);

			String countKey = key + ":" + MetricType.COUNT.name();
			putKey(datas, values, countKey);

			String sumKey = key + ":" + MetricType.SUM.name();
			putKey(datas, values, sumKey);
		}
		return values;
	}
}
