package com.dianping.cat.report.page.sql;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.consumer.sql.model.entity.Database;
import com.dianping.cat.consumer.sql.model.entity.Method;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.entity.Table;
import com.dianping.cat.consumer.sql.model.transform.BaseVisitor;

public class DisplaySqlReport extends BaseVisitor {
	private String m_sortBy = "name";

	private String m_database = "cat";

	private String m_currentDatabase;

	private List<Table> m_results = new ArrayList<Table>();

	private int m_totalCount;

	private long m_duration;

	public List<Table> getResults() {
		Collections.sort(m_results, new TableCompartor(m_sortBy));
		return m_results;
	}

	public DisplaySqlReport setDatabase(String database) {
		m_database = database;
		return this;
	}

	public DisplaySqlReport setDuration(long duration) {
		m_duration = duration;
		return this;
	}

	public DisplaySqlReport setSortBy(String sortBy) {
		if (!StringUtils.isEmpty(sortBy)) {
			m_sortBy = sortBy;
		}
		return this;
	}

	@Override
	public void visitDatabase(Database database) {
		m_currentDatabase = database.getId();
		super.visitDatabase(database);
	}

	@Override
	public void visitSqlReport(SqlReport sqlReport) {
		super.visitSqlReport(sqlReport);

		for (Table table : m_results) {
			int totalCount = table.getTotalCount();
			table.setTotalPercent(totalCount / (double) m_totalCount);

			for (Method method : table.getMethods().values()) {
				method.setTotalPercent(method.getTotalCount() / (double) totalCount);
			}
		}
	}

	@Override
	public void visitTable(Table table) {
		if (m_database.equals(m_currentDatabase)) {
			m_results.add(table);
			if (table.getId().equals("All")) {
				m_totalCount = table.getTotalCount();
				table.setTotalPercent(1);
			}
			if (m_duration > 0) {
				table.setTps(table.getTotalCount() * 1000.0 / (double) m_duration);

				for (Method method : table.getMethods().values()) {
					method.setTps(method.getTotalCount() * 1000.0 / (double) m_duration);
				}
			}
		}
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
