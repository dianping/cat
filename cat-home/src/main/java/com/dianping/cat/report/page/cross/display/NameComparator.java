package com.dianping.cat.report.page.cross.display;

import java.util.Comparator;

public class NameComparator implements Comparator<NameDetailInfo> {

	private String m_sorted;

	public NameComparator(String sort) {
		m_sorted = sort;
	}

	@Override
	public int compare(NameDetailInfo m1, NameDetailInfo m2) {
		if (m1.getId() != null && m1.getId().startsWith("All")) {
			return -1;
		}
		if (m2.getId() != null && m2.getId().startsWith("All")) {
			return 1;
		}

		if (m_sorted.equals("name")) {
			return m1.getId().compareTo(m2.getId());
		}
		if (m_sorted.equals("total")) {
			return (int) (m2.getTotalCount() - m1.getTotalCount());
		}
		if (m_sorted.equals("failure")) {
			return (int) (m2.getFailureCount() - m1.getFailureCount());
		}
		if (m_sorted.equals("failurePercent")) {
			return (int) (m2.getFailurePercent() * 1000 - m1.getFailurePercent() * 1000);
		}
		if (m_sorted.equals("avg")) {
			return (int) (m2.getAvg() * 1000 - m1.getAvg() * 1000);
		}
		return 0;
	}
}