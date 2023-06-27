package com.dianping.cat.report.page.crash.display;

import java.util.Comparator;

public class CrashReportSorter implements Comparator<DisplayVersion> {

	private String m_sort;

	public CrashReportSorter(String sort) {
		m_sort = sort;
	}

	@Override
	public int compare(DisplayVersion o1, DisplayVersion o2) {
		SortType sortType = SortType.findByName(m_sort);

		switch (sortType) {
		case COUNT:
			return o2.getCrashCount() - o1.getCrashCount();
		case COUNT_MOM:
			if (o1.getCrashCountMoM() >= o2.getCrashCountMoM()) {
				return -1;
			} else {
				return 1;
			}
		case COUNT_YOY:
			if (o1.getCrashCountYoY() >= o2.getCrashCountYoY()) {
				return -1;
			} else {
				return 1;
			}
		case PERCENT:
			if (o1.getPercent() >= o2.getPercent()) {
				return -1;
			} else {
				return 1;
			}
		case PERCENT_MOM:
			if (o1.getPercentMoM() >= o2.getPercentMoM()) {
				return -1;
			} else {
				return 1;
			}
		case PERCENT_YOY:
			if (o1.getPercentYoY() >= o2.getPercentYoY()) {
				return -1;
			} else {
				return 1;
			}
		case DAU:
			return o2.getDau() - o1.getDau();
		case VERSION:
			return o2.getId().compareTo(o1.getId());
		}

		return 0;
	}

}
