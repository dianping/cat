package com.dianping.cat.report.page.app.display;

import com.dianping.cat.report.graph.DistributeDetailInfo;
import com.dianping.cat.report.graph.PieChart;

public class AppConnectionDisplayInfo {

	private PieChart m_pieChart;

	private DistributeDetailInfo m_pieChartDetailInfo;

	public PieChart getPieChart() {
		return m_pieChart;
	}

	public void setPieChart(PieChart pieChart) {
		m_pieChart = pieChart;
	}

	public DistributeDetailInfo getPieChartDetailInfo() {
		return m_pieChartDetailInfo;
	}

	public void setPieChartDetailInfo(DistributeDetailInfo pieChartDetailInfo) {
		m_pieChartDetailInfo = pieChartDetailInfo;
	}
}
