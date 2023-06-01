package com.dianping.cat.report.page.browser.display;

import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.DistributeDetailInfo;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.graph.PieChart;

import java.util.List;
import java.util.Map;

public class AjaxDataDisplayInfo {

	private LineChart m_lineChart;

	private PieChart m_pieChart;

	private BarChart m_barChart;

	private DistributeDetailInfo m_distributeDetailInfos;

	private Map<String, AjaxDataDetail> m_comparisonAjaxDetails;

	private List<AjaxDataDetail> m_ajaxDataDetailInfos;

	public List<AjaxDataDetail> getAjaxDataDetailInfos() {
		return m_ajaxDataDetailInfos;
	}

	public Map<String, AjaxDataDetail> getComparisonAjaxDetails() {
		return m_comparisonAjaxDetails;
	}

	public LineChart getLineChart() {
		return m_lineChart;
	}

	public PieChart getPieChart() {
		return m_pieChart;
	}

	public BarChart getBarChart() {
		return m_barChart;
	}

	public DistributeDetailInfo getDistributeDetailInfos() {
		return m_distributeDetailInfos;
	}

	public void setAjaxDataDetailInfos(List<AjaxDataDetail> ajaxDataDetailInfos) {
		m_ajaxDataDetailInfos = ajaxDataDetailInfos;
	}

	public void setComparisonAjaxDetails(Map<String, AjaxDataDetail> comparisonAjaxDetail) {
		m_comparisonAjaxDetails = comparisonAjaxDetail;
	}

	public void setLineChart(LineChart lineChart) {
		m_lineChart = lineChart;
	}

	public void setPieChart(PieChart pieChart) {
		m_pieChart = pieChart;
	}

	public void setBarChart(BarChart barChart) {
		m_barChart = barChart;
	}

	public void setDistributeDetailInfos(DistributeDetailInfo distributeDetailInfos) {
		m_distributeDetailInfos = distributeDetailInfos;
	}

}
