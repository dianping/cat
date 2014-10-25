package com.dianping.cat.report.page.cross.display;

import java.util.Comparator;

public class TypeComparator implements Comparator<TypeDetailInfo> {

	private String m_sorted;

	public TypeComparator(String sort) {
		m_sorted = sort;
	}

	@Override
	public int compare(TypeDetailInfo m1, TypeDetailInfo m2) {
		if (m1.getProjectName() != null && m1.getProjectName().startsWith("All")) {
			return -1;
		}
		if (m2.getProjectName() != null && m2.getProjectName().startsWith("All")) {
			return 1;
		}
		if (m1.getIp() != null && m1.getIp().startsWith("All")) {
			return -1;
		}
		if (m2.getIp() != null && m2.getIp().startsWith("All")) {
			return 1;
		}
		if (m_sorted.equals("name")) {
			if (m1.getProjectName() != null && m2.getProjectName() != null) {
				if (!m1.getProjectName().equals(m2.getProjectName())) {
					return m1.getProjectName().compareTo(m2.getProjectName());
				}
			}
			if (m1.getIp() != null && m2.getIp() != null) {
				if (!m1.getIp().equals(m2.getIp())) {
					return m1.getIp().compareTo(m2.getIp());
				}
			}
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