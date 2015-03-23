package com.dianping.cat.report.page.app.display;

import java.util.Comparator;

import com.dianping.cat.report.page.app.service.AppDataService;

public class ChartSorter {

	private String m_sortBy;

	public ChartSorter() {
		this(AppDataService.REQUEST);
	}

	public ChartSorter(String sortBy) {
		m_sortBy = sortBy;
	}

	public Comparator<AppDataDetail> buildLineChartInfoComparator() {

		return new LineChartDetailInfoComparator();
	}

	public Comparator<PieChartDetailInfo> buildPieChartInfoComparator() {

		return new PieChartDetailInfoComparator();
	}

	public class LineChartDetailInfoComparator implements Comparator<AppDataDetail> {

		@Override
		public int compare(AppDataDetail o1, AppDataDetail o2) {
			if (AppDataService.SUCCESS.equals(m_sortBy)) {
				return (int) ((o2.getSuccessRatio() - o1.getSuccessRatio()) * 1000);
			} else if (AppDataService.REQUEST.equals(m_sortBy)) {
				return (int) (o2.getAccessNumberSum() - o1.getAccessNumberSum());
			} else if (AppDataService.DELAY.equals(m_sortBy)) {
				return (int) ((o2.getResponseTimeAvg() - o1.getResponseTimeAvg()) * 1000);
			} else if (AppDataService.REQUEST_PACKAGE.equals(m_sortBy)) {
				return (int) ((o2.getRequestPackageAvg() - o1.getRequestPackageAvg()) * 1000);
			} else if (AppDataService.RESPONSE_PACKAGE.equals(m_sortBy)) {
				return (int) ((o2.getResponsePackageAvg() - o1.getResponsePackageAvg()) * 1000);
			} else {
				return 0;
			}
		}
	}

	public class PieChartDetailInfoComparator implements Comparator<PieChartDetailInfo> {

		@Override
		public int compare(PieChartDetailInfo o1, PieChartDetailInfo o2) {
			return (int) (o2.getRequestSum() - o1.getRequestSum());
		}
	}

}
