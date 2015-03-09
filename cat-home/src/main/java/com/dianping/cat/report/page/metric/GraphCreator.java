package com.dianping.cat.report.page.metric;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.company.model.entity.ProductLine;
import com.dianping.cat.consumer.config.ProductLineConfig;
import com.dianping.cat.consumer.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.config.entity.Tag;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.metric.AbstractGraphCreator;
import com.dianping.cat.report.alert.AlertInfo.AlertMetric;
import com.dianping.cat.report.alert.MetricType;
import com.dianping.cat.service.ProjectService;

public class GraphCreator extends AbstractGraphCreator {

	@Inject
	private ProjectService m_projectService;

	private Map<String, LineChart> buildChartData(String productLine, final Map<String, double[]> datas, Date startDate,
	      Date endDate, final Map<String, double[]> dataWithOutFutures) {
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		List<AlertMetric> alertKeys = m_alertInfo.queryLastestAlarmKey(5);
		int step = m_dataExtractor.getStep();

		for (Entry<String, double[]> entry : dataWithOutFutures.entrySet()) {
			String key = entry.getKey();
			double[] value = entry.getValue();
			LineChart lineChart = new LineChart();

			buildLineChartTitle(alertKeys, lineChart, key);
			lineChart.setStart(startDate);
			lineChart.setSize(value.length);
			lineChart.setStep(step * TimeHelper.ONE_MINUTE);
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

		return buildChartData(productLine, oldCurrentValues, startDate, endDate, dataWithOutFutures);
	}

	protected String buildContactInfo(String domainName) {
		try {
			Project project = m_projectService.findByDomain(domainName);

			if (project != null) {
				String owners = project.getOwner();
				String phones = project.getPhone();
				StringBuilder builder = new StringBuilder();

				builder.append("[项目: ").append(domainName);
				if (!StringUtils.isEmpty(owners)) {
					builder.append(" 负责人: ").append(owners);
				}
				if (!StringUtils.isEmpty(phones)) {
					builder.append(" 手机: ").append(phones).append(" ]");
				}
				return builder.toString();
			}
		} catch (Exception ex) {
			Cat.logError("build contact info error for doamin: " + domainName, ex);
		}

		return null;
	}

	public Map<String, LineChart> buildDashboardByTag(Date start, Date end, String tag) {
		Map<String, LineChart> result = new LinkedHashMap<String, LineChart>();
		List<MetricItemConfig> metricItemConfigs = m_metricConfigManager.queryMetricItemConfigs(tag);
		Collection<ProductLine> productLines = m_productLineConfigManager.queryAllProductLines().values();
		Map<String, LineChart> allCharts = new LinkedHashMap<String, LineChart>();

		for (ProductLine productLine : productLines) {
			if (isProductLineInTag(productLine.getId(), metricItemConfigs)) {
				allCharts.putAll(buildChartsByProductLine(productLine.getId(), start, end));
			}
		}
		for (MetricItemConfig metric : metricItemConfigs) {
			String domain = metric.getDomain();
			String metricType = metric.getType();
			String type = metricType.equalsIgnoreCase("metric") ? "Metric" : metricType;
			String key = metric.getMetricKey();

			for (Tag metricTag : metric.getTags()) {
				if (tag.equals(metricTag.getName())) {
					String tagType = metricTag.getType();
					String id = m_metricConfigManager.buildMetricKey(domain, type, key) + ":" + tagType;

					put(allCharts, result, id);
				}
			}
		}
		return result;
	}

	private Map<String, double[]> buildGraphData(MetricReport metricReport, List<MetricItemConfig> metricConfigs) {
		Map<String, double[]> datas = m_pruductDataFetcher.buildGraphData(metricReport);
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

	private void buildLineChartTitle(List<AlertMetric> alertKeys, LineChart chart, String key) {
		int index = key.lastIndexOf(":");
		String metricId = key.substring(0, index);
		String type = key.substring(index + 1);
		MetricItemConfig config = m_metricConfigManager.queryMetricItemConfig(metricId);

		if (config != null) {
			String des = queryMetricItemDes(type);
			String title = config.getTitle() + des;

			chart.setTitle(title);
			chart.setId(metricId + ":" + type);

			if (containMetric(alertKeys, metricId)) {
				String contactInfo = buildContactInfo(extractDomain(key));

				chart.setHtmlTitle("<span style='color:red'>" + title + "<br><small>" + contactInfo + "</small></span>");
			} else {
				chart.setHtmlTitle(title);
			}
		}
	}

	private boolean containMetric(List<AlertMetric> alertKeys, String metricId) {
		for (AlertMetric alertMetric : alertKeys) {
			if (alertMetric.getMetricId().equals(metricId)) {
				return true;
			}
		}

		return false;
	}

	private String extractDomain(String metricKey) {
		try {
			return metricKey.split(":")[0];
		} catch (Exception ex) {
			Cat.logError("extract domain error:" + metricKey, ex);
			return null;
		}
	}

	private boolean isProductLineInTag(String productLine, List<MetricItemConfig> configs) {
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine,
		      ProductLineConfig.METRIC);
		List<MetricItemConfig> metricConfig = m_metricConfigManager.queryMetricItemConfigs(domains);

		for (MetricItemConfig metric : configs) {
			String domain = metric.getDomain();
			String type = metric.getType();
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

	private Map<String, double[]> prepareAllData(String productLine, Date startDate, Date endDate) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeHelper.ONE_MINUTE);
		Map<String, double[]> oldCurrentValues = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeHelper.ONE_HOUR) {
			Map<String, double[]> currentValues = queryMetricValueByDate(productLine, start);

			mergeMap(oldCurrentValues, currentValues, totalSize, index);
			index++;
		}
		return oldCurrentValues;
	}

	private String queryMetricItemDes(String type) {
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

	private Map<String, double[]> queryMetricValueByDate(String productLine, long start) {
		MetricReport metricReport = m_metricReportService.queryMetricReport(productLine, new Date(start));
		List<String> domains = m_productLineConfigManager.queryDomainsByProductLine(productLine,
		      ProductLineConfig.METRIC);
		List<MetricItemConfig> metricConfigs = m_metricConfigManager.queryMetricItemConfigs(domains);

		Collections.sort(metricConfigs, new Comparator<MetricItemConfig>() {
			@Override
			public int compare(MetricItemConfig o1, MetricItemConfig o2) {
				return (int) (o1.getViewOrder() * 100 - o2.getViewOrder() * 100);
			}
		});
		Map<String, double[]> currentValues = buildGraphData(metricReport, metricConfigs);
		double sum = 0;

		for (Entry<String, double[]> entry : currentValues.entrySet()) {
			double[] value = entry.getValue();
			int length = value.length;

			for (int i = 0; i < length; i++) {
				sum = sum + value[i];
			}
		}
		return currentValues;
	}
}
