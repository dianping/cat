package com.dianping.cat.report.page.sql;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import com.dianping.cat.job.sql.dal.SqlReportRecord;

public class SqlReport {
	private Date m_startTime;

	private Date m_endTime;

	private List<String> m_domains;

	private String m_domain;

	private String m_sortBy;

	private List<SqlReportModel> m_reportRecords;

	public String getSortBy() {
		return m_sortBy;
	}

	public void setSortBy(String sortBy) {
		m_sortBy = sortBy;
	}

	public Date getStartTime() {
		return m_startTime;
	}

	public SqlReport setStartTime(Date startTime) {
		m_startTime = startTime;
		return this;
	}

	public Date getEndTime() {
		return m_endTime;
	}

	public SqlReport setEndTime(Date endTime) {
		m_endTime = endTime;
		return this;
	}

	public List<String> getDomains() {
		return m_domains;
	}

	public SqlReport setDomains(List<String> domains) {
		m_domains = domains;
		return this;
	}

	public String getDomain() {
		return m_domain;
	}

	public SqlReport setDomain(String domain) {
		m_domain = domain;
		return this;
	}

	public List<SqlReportModel> getReportRecords() {
		if (m_sortBy == null) {
			m_sortBy = "avg";
		}
		Collections.sort(m_reportRecords, new SqlReportModelCompartor(m_sortBy));
		return m_reportRecords;
	}

	public void setReportRecords(List<SqlReportModel> reportRecords) {
		m_reportRecords = reportRecords;
	}

	public static class SqlReportModelCompartor implements Comparator<SqlReportModel> {
		private String m_sorted;

		public SqlReportModelCompartor(String sortBy) {
			m_sorted = sortBy;
		}

		@Override
		public int compare(SqlReportModel m1, SqlReportModel m2) {
			SqlReportRecord record1 = m1.getRecord();

			SqlReportRecord record2 = m2.getRecord();
			if (m_sorted.equals("name")) {
				return record1.getName().compareTo(record2.getName());
			}
			if (m_sorted.equals("total")) {
				return (int) (record1.getTotalCount() - record2.getTotalCount());
			}
			if (m_sorted.equals("failure")) {
				return (int) (record1.getFailureCount() - record2.getFailureCount());
			}
			if (m_sorted.equals("failurePercent")) {
				return (int) (m1.getFailurePercent() * 100 - m2.getFailurePercent() * 100);
			}
			if (m_sorted.equals("avg")) {
				return (int) (m1.getAvg() * 100 - m2.getAvg() * 100);
			}
			if (m_sorted.equals("longsql")) {
				return (int) (record1.getLongSqls() - record2.getLongSqls());
			}
			if (m_sorted.equals("longsqlPercent")) {
				return (int) (m1.getLongPercent() * 100 - m2.getLongPercent() * 100);
			}
			return 0;
		}
	}

}
