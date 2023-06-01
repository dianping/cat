package com.dianping.cat.report.page.app.display;

import com.dianping.cat.configuration.app.speed.entity.Speed;
import com.dianping.cat.helper.JsonBuilder;
import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.LineChart;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

public class AppSpeedDisplayInfo {

	private LineChart m_lineChart;

	private Map<String, AppSpeedDetail> m_appSpeedSummarys;

	private Map<String, List<AppSpeedDetail>> m_appSpeedDetails = new LinkedHashMap<String, List<AppSpeedDetail>>();

	private Map<String, List<Speed>> m_speeds;

	private BarChart m_cityChart;

	private BarChart m_operatorChart;

	private BarChart m_versionChart;

	private BarChart m_platformChart;

	private BarChart m_networkChart;

	public void addDetail(String key, List<AppSpeedDetail> details) {
		m_appSpeedDetails.put(key, details);
	}

	public Map<String, List<AppSpeedDetail>> getAppSpeedBarDetails() {
		return m_appSpeedDetails;
	}

	public Map<String, List<Speed>> getSpeeds() {
		return m_speeds;
	}

	public BarChart getCityChart() {
		return m_cityChart;
	}

	public void setCityChart(BarChart cityChart) {
		m_cityChart = cityChart;
	}

	public BarChart getOperatorChart() {
		return m_operatorChart;
	}

	public void setOperatorChart(BarChart operatorChart) {
		m_operatorChart = operatorChart;
	}

	public BarChart getVersionChart() {
		return m_versionChart;
	}

	public void setVersionChart(BarChart versionChart) {
		m_versionChart = versionChart;
	}

	public BarChart getPlatformChart() {
		return m_platformChart;
	}

	public void setPlatformChart(BarChart platformChart) {
		m_platformChart = platformChart;
	}

	public BarChart getNetworkChart() {
		return m_networkChart;
	}

	public void setNetworkChart(BarChart networkChart) {
		m_networkChart = networkChart;
	}

	public void setSpeeds(Map<String, List<Speed>> speeds) {
		m_speeds = speeds;
	}

	public String getPage2StepsJson() {
		return new JsonBuilder().toJson(m_speeds);
	}

	public Set<String> getPages() {
		return m_speeds.keySet();
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public void setAppSpeedDetails(Map<String, List<AppSpeedDetail>> appSpeedDetails) {
		m_appSpeedDetails = appSpeedDetails;
	}

	public void setAppSpeedSummarys(Map<String, AppSpeedDetail> appSpeedSummarys) {
		m_appSpeedSummarys = appSpeedSummarys;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}

	public Map<String, Map<Integer, AppSpeedDetail>> getAppSpeedSummarys() {
		Map<String, Map<Integer, AppSpeedDetail>> map = new LinkedHashMap<String, Map<Integer, AppSpeedDetail>>();

		if (m_appSpeedSummarys != null && !m_appSpeedSummarys.isEmpty()) {
			for (Entry<String, AppSpeedDetail> entry : m_appSpeedSummarys.entrySet()) {
				Map<Integer, AppSpeedDetail> m = new LinkedHashMap<Integer, AppSpeedDetail>();
				AppSpeedDetail d = entry.getValue();

				m.put(d.getMinuteOrder(), d);
				map.put(entry.getKey(), m);
			}
		}
		return map;
	}

	public Map<String, Map<Integer, AppSpeedDetail>> getAppSpeedDetails() {
		Map<String, Map<Integer, AppSpeedDetail>> map = new LinkedHashMap<String, Map<Integer, AppSpeedDetail>>();

		if (m_appSpeedDetails != null && !m_appSpeedDetails.isEmpty()) {
			for (Entry<String, List<AppSpeedDetail>> entry : m_appSpeedDetails.entrySet()) {
				Map<Integer, AppSpeedDetail> m = new LinkedHashMap<Integer, AppSpeedDetail>();

				for (AppSpeedDetail detail : entry.getValue()) {
					m.put(detail.getMinuteOrder(), detail);
				}
				map.put(entry.getKey(), m);
			}
		}
		return map;
	}
}
