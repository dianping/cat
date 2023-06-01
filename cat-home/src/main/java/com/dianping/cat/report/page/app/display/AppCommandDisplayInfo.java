package com.dianping.cat.report.page.app.display;

import com.dianping.cat.report.graph.BarChart;
import com.dianping.cat.report.graph.DistributeDetailInfo;
import com.dianping.cat.report.graph.PieChart;

public class AppCommandDisplayInfo {

	private DistributeDetailInfo m_distributeDetails;

	private PieChart m_pieChart;

	private BarChart m_barChart;

	public BarChart getBarChart() {
		return m_barChart;
	}

	public DistributeDetailInfo getDistributeDetails() {
		return m_distributeDetails;
	}

	public PieChart getPieChart() {
		return m_pieChart;
	}

	public void setBarChart(BarChart barChart) {
		m_barChart = barChart;
	}

	public void setDistributeDetails(DistributeDetailInfo distributeDetails) {
		m_distributeDetails = distributeDetails;
	}

	public void setPieChart(PieChart pieChart) {
		m_pieChart = pieChart;
	}

}
