package com.dianping.cat.report.chart;

import java.util.ArrayList;
import java.util.Date;
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
import com.dianping.cat.report.task.metric.MetricType;

public class NetworkGraphCreator extends GraphCreator {

	private static String IN_KEY_SUFFIX = "-in";

	private static String OUT_KEY_SUFFIX = "-out";

	private static String FLOW_TITLE_SUFFIX = "-flow";

	private static String IN_ERRORS_KEY_SUFFIX = "-inerrors";

	private static String OUT_ERRORS_KEY_SUFFIX = "-outerrors";

	private static String IN_DISCARDS_KEY_SUFFIX = "-indiscards";

	private static String OUT_DISCARDS_KEY_SUFFIX = "-outdiscards";

	private static String PACKAGE_TITLE_SUFFIX = "-package";

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
					buildLineChartTitle(alertItems, lineChart, key);

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
		Set<String> keySet = new LinkedHashSet<String>();

		for (String key : keys) {
			int colonIndex = key.lastIndexOf(":");
			String tmp = key.substring(0, colonIndex > -1 ? colonIndex : 0);
			
			int hyphenIndex = tmp.lastIndexOf("-");
			keySet.add(tmp.substring(0, hyphenIndex > -1 ? hyphenIndex : 0));
		}

		Map<String, List<String>> aggregationKeys = new LinkedHashMap<String, List<String>>();

		for (String key : keySet) {
			String inFlowKey = key + IN_KEY_SUFFIX;
			String outFlowKey = key + OUT_KEY_SUFFIX;
			String flowTitle = key + FLOW_TITLE_SUFFIX;
			List<String> flowKeys = new ArrayList<String>();

			flowKeys.add(inFlowKey);
			flowKeys.add(outFlowKey);
			aggregationKeys.putAll(buildKeys(flowTitle, flowKeys));

			String inErrorKey = key + IN_ERRORS_KEY_SUFFIX;
			String outErrorKey = key + OUT_ERRORS_KEY_SUFFIX;
			String inDiscardsKey = key + IN_DISCARDS_KEY_SUFFIX;
			String outDiscardsKey = key + OUT_DISCARDS_KEY_SUFFIX;
			String discardsTitle = key + PACKAGE_TITLE_SUFFIX;
			List<String> discardsKeys = new ArrayList<String>();

			discardsKeys.add(inErrorKey);
			discardsKeys.add(outErrorKey);
			discardsKeys.add(inDiscardsKey);
			discardsKeys.add(outDiscardsKey);
			aggregationKeys.putAll(buildKeys(discardsTitle, discardsKeys));
		}

		return aggregationKeys;
	}

}
