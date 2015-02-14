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
import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.consumer.metric.model.entity.Statistic;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.metric.AbstractGraphCreator;

public class SystemGraphCreator extends AbstractGraphCreator {

	public static final String PAAS_SYSTEM = "paasSystem";

	public static final String SYSTEM_TYPE = "system";

	public static final String JVM_TYPE = "jvm";

	public static final String NGINX_TYPE = "nginx";

	private static final List<String> SYSTEM_KEY_LIST = Arrays.asList("sysCpu:avg", "iowaitCpu:avg", "niceCpu:avg",
	      "stealCpu:avg", "userCpu:avg", "softirqCpu:avg", "idleCpu:avg", "irqCpu:avg", "/-usage:avg",
	      "/-freeInodes:avg", "/-read:sum", "/-write:sum", "/data-usage:avg", "/data-freeInodes:avg", "/data-read:sum",
	      "/data-write:sum", "/usr-usage:avg", "/usr-freeInodes:avg", "/usr-read:sum", "/usr-write:sum",
	      "/var-usage:avg", "/var-freeInodes:avg", "/var-read:sum", "/var-write:sum", "eth0-inFlow:sum",
	      "eth0-outFlow:sum", "eth0-dropped:sum", "eth0-errors:sum", "eth0-collisions:sum", "lo-inFlow:sum",
	      "lo-outFlow:sum", "swapUsage:avg", "loadAvg1:avg", "loadAvg5:avg", "totalMem:avg", "usedMem:avg",
	      "freeMem:avg", "sharedMem:avg", "buffersMem:avg", "cachedMem:avg", "totalProcess:avg", "runningProcess:avg",
	      "swapUsage:avg", "establishedTcp:avg", "loginUsers:avg");

	private static final List<String> PAAS_SYSTEM_KEY_LIST = Arrays.asList("sysCpu:avg", "userCpu:avg", "cpuUsage:avg",
	      "/-usage:avg", "/-freeInodes:avg", "/-read:sum", "/-write:sum", "eth0-inFlow:sum", "eth0-outFlow:sum",
	      "eth0-dropped:sum", "eth0-errors:sum", "eth0-collisions:sum", "lo-inFlow:sum", "lo-outFlow:sum",
	      "swapUsage:avg", "totalMem:avg", "usedMem:avg", "freeMem:avg", "sharedMem:avg", "buffersMem:avg",
	      "cachedMem:avg", "totalProcess:avg", "runningProcess:avg", "swapUsage:avg", "establishedTcp:avg",
	      "loginUsers:avg");

	private static final List<String> JVM_KEY_LIST = new ArrayList<String>(Arrays.asList("edenUsage:avg",
	      "oldUsage:avg", "permUsage:avg", "catalinaLogSize:sum"));

	private static final List<String> NGINX_KEY_LIST = new ArrayList<String>();

	public Map<String, LineChart> buildChartsByProductLine(String productLine, Map<String, String> pars,
	      Set<String> ipAddrs, Date startDate, Date endDate) {
		String type = filterType(pars);
		Map<String, double[]> oldCurrentValues = prepareAllData(productLine, pars, ipAddrs, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);
		Set<String> curIpAddrs = buildIpAddrs(pars.get("ip"), ipAddrs);
		Map<String, Map<String, String>> aggregationKeys = buildLineChartKeys(dataWithOutFutures.keySet(), curIpAddrs,
		      type);

		return buildChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures, aggregationKeys);
	}

	private String filterType(Map<String, String> pars) {
		String type = pars.get("type");

		if (PAAS_SYSTEM.equals(type)) {
			type = PAAS_SYSTEM;

			pars.put("type", SYSTEM_TYPE);
		}
		return type;
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

	public Map<String, double[]> prepareAllData(String group, Map<String, String> pars, Set<String> ipAddrs,
	      Date startDate, Date endDate) {
		long start = startDate.getTime(), end = endDate.getTime();
		int totalSize = (int) ((end - start) / TimeHelper.ONE_MINUTE);
		Map<String, double[]> sourceValue = new LinkedHashMap<String, double[]>();
		int index = 0;

		for (; start < end; start += TimeHelper.ONE_HOUR) {
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
			lineChart.setUnit(buildUnit(chartTitle));
			lineChart.setStep(step * TimeHelper.ONE_MINUTE);

			if (keyMapEntry.getValue().entrySet().isEmpty()) {
				lineChart.add("none", buildNoneData(startDate, endDate, 1));
			}

			for (Entry<String, String> ip2MetricKey : keyMapEntry.getValue().entrySet()) {
				String key = ip2MetricKey.getValue();
				String lineTitle = ip2MetricKey.getKey();

				if (dataWithOutFutures.containsKey(key)) {
					Map<Long, Double> all = convertToMap(datas.get(key), startDate, 1);
					Map<Long, Double> current = convertToMap(dataWithOutFutures.get(key), startDate, step);

					addLastMinuteData(current, all, m_lastMinute, endDate);
					convertFlowMetric(lineChart, current, lineTitle);
				} else {
					lineChart.add(lineTitle, buildNoneData(startDate, endDate, 1));
				}
			}
			charts.put(chartTitle, lineChart);
		}
		return charts;
	}

	protected List<String> fetchExpectedKeys(String type) {
		if (PAAS_SYSTEM.equalsIgnoreCase(type)) {
			return PAAS_SYSTEM_KEY_LIST;
		} else if (SYSTEM_TYPE.equalsIgnoreCase(type)) {
			return SYSTEM_KEY_LIST;
		} else if (JVM_TYPE.equalsIgnoreCase(type)) {
			return JVM_KEY_LIST;
		} else if (NGINX_TYPE.equalsIgnoreCase(type)) {
			return NGINX_KEY_LIST;
		} else {
			return new ArrayList<String>();
		}
	}

	private Map<String, Map<String, String>> buildLineChartKeys(Set<String> Allkeys, Set<String> ipAddrs, String type) {
		List<String> expectedKeys = fetchExpectedKeys(type);
		Map<String, Map<String, String>> aggregationKeys = new LinkedHashMap<String, Map<String, String>>();

		for (String expectedKey : expectedKeys) {
			int typeIndex = expectedKey.lastIndexOf(":");
			String metricType = expectedKey.substring(typeIndex + 1);
			String headKey = expectedKey.substring(0, typeIndex);
			Set<String> pidSuffixs = new HashSet<String>();

			if (JVM_TYPE.equalsIgnoreCase(type)) {
				for (String key : Allkeys) {
					String prefix = headKey + "@";
					if (key.startsWith(prefix)) {
						String pid = key.substring(key.indexOf("@") + 1, key.lastIndexOf("_"));
						String pidSuffix = "@" + pid;
						pidSuffixs.add(pidSuffix);
					}
				}
			}
			Map<String, String> ipMap = findOrCreate(headKey, aggregationKeys);

			for (String ip : ipAddrs) {
				if (pidSuffixs.size() <= 1) {
					ipMap.put(ip, headKey + "_" + ip + ":" + metricType.toUpperCase());
				} else {
					for (String suffix : pidSuffixs) {
						ipMap.put(ip + suffix, headKey + suffix + "_" + ip + ":" + metricType.toUpperCase());
					}
				}
			}
		}
		return aggregationKeys;
	}

	private Map<String, String> findOrCreate(String key, Map<String, Map<String, String>> aggregationKeys) {
		Map<String, String> ipMap = aggregationKeys.get(key);

		if (ipMap == null) {
			ipMap = new HashMap<String, String>();

			aggregationKeys.put(key, ipMap);
		}
		return ipMap;
	}
}
