package com.dianping.cat.report.page.app.display;

import java.util.List;
import java.util.Map;

import com.dianping.cat.report.graph.LineChart;

public class AppSpeedDisplayInfo {

	private LineChart m_lineChart;

	private Map<String, AppSpeedDetail> m_appSpeedSummarys;

	private Map<String, List<AppSpeedDetail>> m_appSpeedDetails;

	public Map<String, List<AppSpeedDetail>> getAppSpeedDetails() {
		return m_appSpeedDetails;
	}

	public Map<String, AppSpeedDetail> getAppSpeedSummarys() {
		return m_appSpeedSummarys;
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
}
