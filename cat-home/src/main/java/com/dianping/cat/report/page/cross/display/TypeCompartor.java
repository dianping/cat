package com.dianping.cat.report.page.cross.display;

import java.util.Comparator;

public class TypeCompartor implements Comparator<TypeDetailInfo> {

	private String m_sorted;

	public TypeCompartor(String sort) {
		m_sorted = sort;
	}

	@Override
	public int compare(TypeDetailInfo m1, TypeDetailInfo m2) {
		if (m1.getProjectName()!=null&&m1.getProjectName().equals("ALL")) {
			return -1;
		}
		if (m2.getProjectName()!=null&&m2.getProjectName().equals("ALL")) {
			return 1;
		}
		if (m1.getIp()!=null&&m1.getIp().equals("ALL")) {
			return -1;
		}
		if (m2.getIp()!=null&&m2.getIp().equals("ALL")) {
			return 1;
		}
		if (!m1.getType().equals(m2.getType())) {
			return m1.getType().compareTo(m2.getType());
		}

		if (m_sorted.equals("total")) {
			return (int) (m2.getTotalCount() - m1.getTotalCount());
		}
		if (m_sorted.equals("failure")) {
			return (int) (m2.getFailureCount() - m1.getFailureCount());
		}
		if (m_sorted.equals("failurePercent")) {
			return (int) (m2.getFailurePercent() * 100 - m1.getFailurePercent() * 100);
		}
		if (m_sorted.equals("avg")) {
			return (int) (m2.getAvg() * 100 - m1.getAvg() * 100);
		}
		return 0;
	}
}