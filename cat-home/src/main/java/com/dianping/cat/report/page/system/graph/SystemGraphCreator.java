package com.dianping.cat.report.page.system.graph;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dianping.cat.Constants;
import com.dianping.cat.advanced.metric.config.entity.MetricItemConfig;
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Statistic;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.chart.AbstractGraphCreator;
import com.dianping.cat.report.page.LineChart;

public class SystemGraphCreator extends AbstractGraphCreator {

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Map<String, String> pars,
	      Set<String> ipAddrs, Date startDate, Date endDate) {
		Map<String, double[]> oldCurrentValues = prepareAllData(productLine, pars, ipAddrs, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);
		Set<String> curIpAddrs = buildIpAddrs(pars.get("ip"), ipAddrs);

		return buildChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures, curIpAddrs);
	}

	private Set<String> buildIpAddrs(String ipPar, Set<String> ipAll) {

		if (Constants.ALL.equalsIgnoreCase(ipPar)) {
			return ipAll;
		} else {
			String[] curIpAddrsArray = ipPar.split("_");
			Set<String> curIpAddrs = new HashSet<String>(Arrays.asList(curIpAddrsArray));

			return curIpAddrs;
		}
	}

	private Map<String, double[]> prepareAllData(String group, Map<String, String> pars, Set<String> ipAddrs,
	      Date startDate, Date endDate) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeUtil.ONE_MINUTE);
		Map<String, double[]> sourceValue = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeUtil.ONE_HOUR) {
			MetricReport report = m_metricReportService.querySystemReport(group, pars, new Date(start));
			Map<String, double[]> currentValues = m_pruductDataFetcher.buildLeastGraphData(report);

			mergeMap(sourceValue, currentValues, totalSize, index);
			index++;

			Statistic ipList = report.getStatistics().get(SystemReportConvertor.IP_LIST_KEY);

			if (ipList != null) {
				ipAddrs.addAll(ipList.getStatisticsItems().keySet());
			}
		}
		return sourceValue;
	}

	private Map<String, LineChart> buildChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures, Set<String> ipAddrs) {
		Map<String, Map<String, String>> aggregationKeys = buildLineChartKeys(dataWithOutFutures.keySet(), ipAddrs);
		Map<String, LineChart> charts = new LinkedHashMap<String, LineChart>();
		int step = m_dataExtractor.getStep();

		for (Entry<String, Map<String, String>> keyMapEntry : aggregationKeys.entrySet()) {
			String chartTitle = keyMapEntry.getKey();
			LineChart lineChart = new LineChart();
			lineChart.setTitle(chartTitle);
			lineChart.setHtmlTitle(chartTitle);
			lineChart.setId(chartTitle);
			lineChart.setStart(startDate);
			lineChart.setStep(step * TimeUtil.ONE_MINUTE);

			for (Entry<String, String> ip2MetricKey : keyMapEntry.getValue().entrySet()) {
				String key = ip2MetricKey.getValue();
				String lineTitle = ip2MetricKey.getKey();

				if (dataWithOutFutures.containsKey(key)) {
					Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
					Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

					addLastMinuteData(current, all, m_lastMinute, endDate);
					lineChart.add(lineTitle, current);
				}
			}
			charts.put(chartTitle, lineChart);
		}
		return charts;
	}

	public Map<String, Map<String, String>> buildLineChartKeys(Set<String> keys, Set<String> ipAddrs) {
		Set<String> chartKeys = new HashSet<String>();
		Map<String, Map<String, String>> aggregationKeys = new LinkedHashMap<String, Map<String, String>>();

		for (String key : keys) {
			String[] tmp = key.split("_");

			chartKeys.add(tmp[0]);
		}

		for (String chartKey : chartKeys) {
			Map<String, String> keyMap = aggregationKeys.get(chartKey);

			if (keyMap == null) {
				keyMap = new HashMap<String, String>();

				aggregationKeys.put(chartKey, keyMap);
			}
			for (String ip : ipAddrs) {
				keyMap.put(ip, chartKey + "_" + ip);
			}
		}
		return aggregationKeys;
	}

	@Override
	protected Map<String, double[]> buildGraphData(MetricReport metricReport, List<MetricItemConfig> metricConfigs) {
		throw new RuntimeException("unsupport in system monitor graph!");
	}

}
