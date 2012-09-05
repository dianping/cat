package com.dianping.cat.report.page.database;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.consumer.database.model.entity.Table;
import com.dianping.cat.consumer.database.model.transform.BaseVisitor;

public class DisplayDatabase extends BaseVisitor {
	private String m_sortBy = "name";

	private List<Table> m_results = new ArrayList<Table>();

	public List<Table> getResults() {
		Collections.sort(m_results, new TableCompartor(m_sortBy));
		return m_results;
	}

	public DisplayDatabase setSortBy(String sortBy) {
		if (!StringUtils.isEmpty(sortBy)) {
			m_sortBy = sortBy;
		}
		return this;
	}

	@Override
	public void visitTable(Table table) {
		m_results.add(table);
	}

	public static class TableCompartor implements Comparator<Table> {

		private String m_sorted;

		public TableCompartor(String sort) {
			m_sorted = sort;
		}

		@Override
		public int compare(Table m1, Table m2) {
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
				return (int) (m2.getFailCount() - m1.getFailCount());
			}
			if (m_sorted.equals("failurePercent")) {
				return (int) (m2.getFailPercent() * 1000 - m1.getFailPercent() * 1000);
			}
			if (m_sorted.equals("avg")) {
				return (int) (m2.getAvg() * 1000 - m1.getAvg() * 1000);
			}
			return 0;
		}

	}
}
