package com.dianping.cat.report.chart;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.LineChart;

public class SystemGraphCreator extends MetricGraphCreator {

	private Set<String> m_allIpAddrs = new HashSet<String>();

	private Set<String> m_curIpAddrs = new HashSet<String>();

	private String m_type;

	public void setType(String type) {
		m_type = type;
	}

	public Set<String> getAllIpAddrs() {
		return m_allIpAddrs;
	}

	public void setCurIpAddrs(Set<String> curIpAddrs) {
		m_curIpAddrs = curIpAddrs;
	}

	@Override
	public Map<String, LineChart> buildChartsByProductLine(String productLine, Date startDate, Date endDate) {
		Map<String, double[]> oldCurrentValues = prepareAllData(productLine, startDate, endDate);
		Map<String, double[]> allCurrentValues = m_dataExtractor.extract(oldCurrentValues);
		Map<String, double[]> dataWithOutFutures = removeFutureData(endDate, allCurrentValues);
		Set<String> keys = dataWithOutFutures.keySet();

		for (String key : keys) {
			int index = key.lastIndexOf(":");
			String tmp = key.substring(0, index > 0 ? index : 0);
			int indexIp = tmp.lastIndexOf("_");
			String ip = tmp.substring(indexIp + 1);

			m_allIpAddrs.add(ip);
		}
		return buildChartData(oldCurrentValues, startDate, endDate, dataWithOutFutures);
	}

	@Override
	public Map<String, LineChart> buildChartData(final Map<String, double[]> datas, Date startDate, Date endDate,
	      final Map<String, double[]> dataWithOutFutures) {
		Map<String, Map<String, String>> aggregationKeys = buildLineChartKeys(dataWithOutFutures.keySet());
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

	public Map<String, Map<String, String>> buildLineChartKeys(Set<String> keys) {
		Map<String, SystemMetricKey> systemMetricKeys = buildSystemMeticKeys(keys);
		// Collection<String> ipAddrs = m_curIpAddrs.isEmpty() ? m_allIpAddrs : m_curIpAddrs;
		Set<String> realKeys = new HashSet<String>();

		for (Entry<String, SystemMetricKey> entry : systemMetricKeys.entrySet()) {
			SystemMetricKey systemMetricKey = entry.getValue();

			for (String ip : m_curIpAddrs) {
				if (systemMetricKey.getSystemType().equals(m_type) && systemMetricKey.getIpAddr().equals(ip)) {
					realKeys.add(systemMetricKey.getAggregationKey());
				}
			}
		}

		Map<String, Map<String, String>> result = new HashMap<String, Map<String, String>>();

		for (String realKey : realKeys) {
			if (result.get(realKey) == null) {
				result.put(realKey, new HashMap<String, String>());
			}
			for (String ip : m_curIpAddrs) {
				for (Entry<String, SystemMetricKey> entry : systemMetricKeys.entrySet()) {
					SystemMetricKey systemMetricKey = entry.getValue();

					if (systemMetricKey.getAggregationKey().equals(realKey) && systemMetricKey.getIpAddr().equals(ip)
					      && systemMetricKey.getSystemType().equals(m_type)) {
						result.get(realKey).put(systemMetricKey.getIpAddr(), systemMetricKey.getCompleteKey());
					}
				}
			}
		}

		return result;
	}

	public Map<String, SystemMetricKey> buildSystemMeticKeys(Set<String> keys) {
		Map<String, SystemMetricKey> systemMetricKeys = new HashMap<String, SystemMetricKey>();

		for (String key : keys) {
			int startIndex = key.indexOf(":", key.indexOf(":") + 1) + 1;
			int endIndex = key.lastIndexOf(":");
			String metricType = key.substring(endIndex + 1);

			if (startIndex <= endIndex) {
				String middleKey = key.substring(startIndex, endIndex);
				int indexType = middleKey.indexOf("_");
				String systemType = middleKey.substring(0, indexType > 0 ? indexType : 0);
				int indexIp = middleKey.lastIndexOf("_");
				String ip = middleKey.substring(indexIp + 1);

				if (indexType + 1 <= indexIp) {
					String realKey = middleKey.substring(indexType + 1, indexIp);
					SystemMetricKey syskey = new SystemMetricKey();

					syskey.setAggregationKey(realKey);
					syskey.setSystemType(systemType);
					syskey.setIpAddr(ip);
					syskey.setCompleteKey(key);
					syskey.setMetricType(metricType);
					systemMetricKeys.put(key, syskey);
				}
			}
		}
		return systemMetricKeys;
	}

	public class SystemMetricKey {

		private String m_metricType;

		private String m_systemType;

		private String m_ipAddr;

		private String m_aggregationKey;

		private String m_completeKey;

		public String getMetricType() {
			return m_metricType;
		}

		public void setMetricType(String metricType) {
			m_metricType = metricType;
		}

		public String getSystemType() {
			return m_systemType;
		}

		public void setSystemType(String systemType) {
			m_systemType = systemType;
		}

		public String getIpAddr() {
			return m_ipAddr;
		}

		public void setIpAddr(String ipAddr) {
			m_ipAddr = ipAddr;
		}

		public String getAggregationKey() {
			return m_aggregationKey;
		}

		public void setAggregationKey(String aggregationKey) {
			m_aggregationKey = aggregationKey;
		}

		public String getCompleteKey() {
			return m_completeKey;
		}

		public void setCompleteKey(String completeKey) {
			m_completeKey = completeKey;
		}

	}

}
