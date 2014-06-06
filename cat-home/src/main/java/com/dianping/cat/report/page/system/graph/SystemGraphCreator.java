package com.dianping.cat.report.page.system.graph;

import java.util.ArrayList;
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

	public static final String SYSTEM_TYPE = "system";

	public static final String JVM_TYPE = "jvm";

	public static final String NGINX_TYPE = "nginx";

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Map<String, String> pars,
	      Set<String> ipAddrs, Date startDate, Date endDate) {
		Map<String, double[]> oldCurrentValues = prepareAllData(productLine, pars, ipAddrs, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);

		String type = pars.get("type");
		Set<String> curIpAddrs = buildIpAddrs(pars.get("ip"), ipAddrs);
		Map<String, Map<String, String>> aggregationKeys = buildLineChartKeys(dataWithOutFutures.keySet(), curIpAddrs,
		      type);

		return buildChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures, aggregationKeys);
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
			Map<String, double[]> currentValues = m_pruductDataFetcher.buildGraphData(report);

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
	      final Map<String, double[]> dataWithOutFutures, Map<String, Map<String, String>> aggregationKeys) {

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
				} else {
					lineChart.add(lineTitle, buildNoneData(startDate, endDate, 1));
				}
			}
			charts.put(chartTitle, lineChart);
		}
		return charts;
	}

	private Map<Long, Double> buildNoneData(Date startDate, Date endDate, int step) {
		int n = 0;
		long current = System.currentTimeMillis();

		if (endDate.getTime() > current) {
			n = (int) ((current - startDate.getTime()) / 60000.0);
		} else {
			n = (int) ((endDate.getTime() - startDate.getTime()) / 60000.0);
		}

		double[] noneData = new double[n];
		Map<Long, Double> currentData = convertToMap(noneData, startDate, step);

		return currentData;
	}

	private List<String> fetchSystemKeys(String type) {

		List<String> systemKeys = new ArrayList<String>(Arrays.asList("cpu:avg", "/-usage:avg", "/boot-usage:avg",
		      "/data-usage:avg", "/usr-usage:avg", "/var-usage:avg", "eth0-in-flow:sum", "eth0-out-flow:sum", "swap:avg",
		      "load:avg", "uptime:avg", "Md5Change:avg", "hostNameChange:avg", "hostIpChange:avg"));

		List<String> jvmKeys = new ArrayList<String>(Arrays.asList("jvm_edenUsage:avg", "jvm_oldUsage:avg",
		      "jvm_permUsage:avg", "tomcatLive:avg", "catalinaLogSize:sum"));

		List<String> nginxKeys = new ArrayList<String>();

		if (SYSTEM_TYPE.equalsIgnoreCase(type)) {
			return systemKeys;
		} else if (JVM_TYPE.equalsIgnoreCase(type)) {
			return jvmKeys;
		} else if (NGINX_TYPE.equalsIgnoreCase(type)) {
			return nginxKeys;
		} else {
			return null;
		}
	}

	private Map<String, Map<String, String>> buildLineChartKeys(Set<String> keys, Set<String> ipAddrs, String type) {
		List<String> systemKeys = fetchSystemKeys(type);
		Map<String, Map<String, String>> aggregationKeys = new LinkedHashMap<String, Map<String, String>>();

		for (String key : systemKeys) {
			String[] keyArray = key.split(":");
			String realKey = keyArray[0];
			String metricType = keyArray[1];
			String des = queryMetricItemDes(metricType.toUpperCase());
			String chartKey = realKey + des;
			Map<String, String> ipMap = aggregationKeys.get(chartKey);

			if (ipMap == null) {
				ipMap = new HashMap<String, String>();

				aggregationKeys.put(chartKey, ipMap);
			}
			for (String ip : ipAddrs) {
				ipMap.put(ip, realKey + "_" + ip + ":" + metricType.toUpperCase());
			}
		}

		return aggregationKeys;
	}

	@Override
	protected Map<String, double[]> buildGraphData(MetricReport metricReport, List<MetricItemConfig> metricConfigs) {
		throw new RuntimeException("unsupport in system monitor graph!");
	}

}
