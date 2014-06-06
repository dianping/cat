package com.dianping.cat.report.page.network.graph;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.helper.Chinese;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.LineChart;
import com.dianping.cat.report.page.metric.graph.MetricGraphCreator;
import com.dianping.cat.report.task.alert.MetricType;

public class NetworkGraphCreator extends MetricGraphCreator {
	
	@Override
	public Map<String, LineChart> buildChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures) {
		Map<String, List<String>> aggregationKeys = buildLineChartKeys(dataWithOutFutures.keySet());
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		List<MetricItemConfig> alertItems = m_alertInfo.queryLastestAlarmInfo(5);
		int step = m_dataExtractor.getStep();

		for (Entry<String, List<String>> keyMapEntry : aggregationKeys.entrySet()) {
			String keyTitle = keyMapEntry.getKey();
			String chartTitle = keyTitle.substring(keyTitle.lastIndexOf(":") + 1);
			LineChart lineChart = new LineChart();
			lineChart.setTitle(chartTitle);
			lineChart.setHtmlTitle(chartTitle);
			lineChart.setId(chartTitle);
			lineChart.setStart(startDate);
			lineChart.setStep(step * TimeUtil.ONE_MINUTE);

			for (String key : keyMapEntry.getValue()) {
				if (dataWithOutFutures.containsKey(key)) {
					buildLineChartTitle(alertItems, lineChart, key, chartTitle);

					double[] baselines = queryBaseline(key, startDate, endDate);
					Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
					Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

					addLastMinuteData(current, all, m_lastMinute, endDate);
					lineChart.add(buildLineTitle(key) + Chinese.CURRENT_VALUE, current);
					lineChart.add(buildLineTitle(key) + Chinese.BASELINE_VALUE,
					      convertToMap(m_dataExtractor.extract(baselines), startDate, step));
				}
			}
			charts.put(keyTitle, lineChart);
		}
		return charts;
	}

	protected void buildLineChartTitle(List<MetricItemConfig> alertItems, LineChart chart, String key, String title) {
		int index = key.lastIndexOf(":");
		String metricId = key.substring(0, index);
		MetricItemConfig config = m_metricConfigManager.queryMetricItemConfig(metricId);

		chart.setTitle(title);

		if (alertItems.contains(config)) {
			chart.setHtmlTitle("<span style='color:red'>" + title + "</span>");
		} else {
			chart.setHtmlTitle(title);
		}
	}

	public String buildLineTitle(String lineKey) {
		int colonIndex = lineKey.lastIndexOf(":");
		String tmp = lineKey.substring(0, colonIndex > -1 ? colonIndex : 0);

		return tmp.substring(tmp.lastIndexOf("-") + 1);
	}

	public List<String> findOrCreate(Map<String, List<String>> map, String key) {
		if (map.get(key) == null) {
			List<String> list = new ArrayList<String>();
			map.put(key, list);
		}
		return map.get(key);
	}

	public Map<String, List<String>> buildKeys(String keyTitle, List<String> keys) {
		Map<String, List<String>> aggregationKeys = new LinkedHashMap<String, List<String>>();

		for (String key : keys) {
			MetricItemConfig config = m_metricConfigManager.queryMetricItemConfig(key);
			String avgTitle = keyTitle + Chinese.Suffix_AVG;

			if (config != null && config.getShowAvg()) {
				List<String> keyList = findOrCreate(aggregationKeys, avgTitle);
				String avgKey = key + ":" + MetricType.AVG.name();
				keyList.add(avgKey);
			}
		}

		for (String key : keys) {
			MetricItemConfig config = m_metricConfigManager.queryMetricItemConfig(key);
			String sumTitle = keyTitle + Chinese.Suffix_SUM;

			if (config != null && config.getShowSum()) {
				List<String> keyList = findOrCreate(aggregationKeys, sumTitle);
				String sumKey = key + ":" + MetricType.SUM.name();
				keyList.add(sumKey);
			}
		}

		for (String key : keys) {
			MetricItemConfig config = m_metricConfigManager.queryMetricItemConfig(key);
			String countTitle = keyTitle + Chinese.Suffix_COUNT;

			if (config != null && config.getShowCount()) {
				List<String> keyList = findOrCreate(aggregationKeys, countTitle);
				String countKey = key + ":" + MetricType.COUNT.name();
				keyList.add(countKey);
			}
		}
		return aggregationKeys;
	}

	public Map<String, List<String>> buildLineChartKeys(Set<String> keys) {
		Set<String> groupSet = new LinkedHashSet<String>();
		Set<String> keySet = new HashSet<String>();
		Map<String, List<String>> aggregationKeys = new LinkedHashMap<String, List<String>>();

		for (String key : keys) {
			int colonIndex = key.lastIndexOf(":");
			String tmp = key.substring(0, colonIndex > -1 ? colonIndex : 0);
			keySet.add(tmp);

			int hyphenIndex = tmp.lastIndexOf("-");
			groupSet.add(tmp.substring(0, hyphenIndex > -1 ? hyphenIndex : 0));
		}

		for (String group : groupSet) {
			List<String> keyList = new ArrayList<String>();
			for (String key : keySet) {
				if (key.startsWith(group)) {
					keyList.add(key);
				}
				aggregationKeys.putAll(buildKeys(group, keyList));
			}
		}

		return aggregationKeys;
	}
}
