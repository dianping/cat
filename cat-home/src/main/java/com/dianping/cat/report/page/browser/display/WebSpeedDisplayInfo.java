package com.dianping.cat.report.page.browser.display;

import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.LineChart;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSpeedDisplayInfo {

	private LineChart m_lineChart;

	private BarChart m_cityChart;

	private BarChart m_operatorChart;

	private BarChart m_sourceChart;

	private BarChart m_platformChart;

	private BarChart m_networkChart;

	private Map<String, WebSpeedDetail> m_webSpeedSummarys;

	private Map<String, List<WebSpeedDetail>> m_webSpeedDetails = new HashMap<String, List<WebSpeedDetail>>();

	public void addDetail(String key, List<WebSpeedDetail> details) {
		m_webSpeedDetails.put(key, details);
	}

	public BarChart getCityChart() {
		return m_cityChart;
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public BarChart getNetworkChart() {
		return m_networkChart;
	}

	public BarChart getOperatorChart() {
		return m_operatorChart;
	}

	public BarChart getPlatformChart() {
		return m_platformChart;
	}

	public BarChart getSourceChart() {
		return m_sourceChart;
	}

	public Map<String, List<WebSpeedDetail>> getWebSpeedDetails() {
		return m_webSpeedDetails;
	}

	public Map<String, WebSpeedDetail> getWebSpeedSummarys() {
		return m_webSpeedSummarys;
	}

	public void setCityChart(BarChart cityChart) {
		m_cityChart = cityChart;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}

	public void setNetworkChart(BarChart networkChart) {
		m_networkChart = networkChart;
	}

	public void setOperatorChart(BarChart operatorChart) {
		m_operatorChart = operatorChart;
	}

	public void setPlatformChart(BarChart platformChart) {
		m_platformChart = platformChart;
	}

	public void setSourceChart(BarChart sourceChart) {
		m_sourceChart = sourceChart;
	}

	public void setWebSpeedDetails(Map<String, List<WebSpeedDetail>> webSpeedDetails) {
		m_webSpeedDetails = webSpeedDetails;
	}

	public void setWebSpeedSummarys(Map<String, WebSpeedDetail> webSpeedSummarys) {
		m_webSpeedSummarys = webSpeedSummarys;
	}
}
