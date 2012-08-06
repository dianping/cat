package com.dianping.cat.report.page.monthreport;

import java.util.Date;
import java.util.Map;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.report.page.problem.ProblemStatistics;
import com.dianping.cat.report.page.problem.ProblemStatistics.TypeStatistics;

public class ProjectReport {

	private CacheInfo m_cache = new CacheInfo();

	private BaseInfo m_call = new BaseInfo();

	private int m_days;

	private String m_domain;

	private Date m_end;

	private ProblemInfo m_problem = new ProblemInfo();

	private BaseInfo m_service = new BaseInfo();

	private BaseInfo m_sql = new BaseInfo();

	private Date m_start;

	private BaseInfo m_url = new BaseInfo();

	public CacheInfo getCache() {
		return m_cache;
	}

	public BaseInfo getCall() {
		return m_call;
	}

	public int getDays() {
		return m_days;
	}

	public String getDomain() {
		return m_domain;
	}

	public Date getEnd() {
		return m_end;
	}

	public ProblemInfo getProblem() {
		return m_problem;
	}

	public BaseInfo getService() {
		return m_service;
	}

	public BaseInfo getSql() {
		return m_sql;
	}

	public Date getStart() {
		return m_start;
	}

	public BaseInfo getUrl() {
		return m_url;
	}

	public void visit(ProblemStatistics problemStatistics) {
		m_problem.visit(problemStatistics, m_days);
		m_problem.setLongUrlPercent(m_url.getTotal());
		m_problem.setLongSqlPercent(m_sql.getTotal());
	}

	public void visit(TransactionReport transactionReport) {
		long day = 60 * 60 * 24 * 1000L;
		long startlong = transactionReport.getStartTime().getTime();
		m_start = new Date(startlong - startlong % day);
		m_end = transactionReport.getEndTime();
		m_days = (int) ((m_end.getTime() - m_start.getTime()) / day) + 1;
		m_domain = transactionReport.getDomain();

		Machine machine = transactionReport.getMachines().get("All");
		Map<String, TransactionType> types = machine.getTypes();

		TransactionType url = types.get("URL");
		if (url != null) {
			m_url.visit(url, m_days);
		}

		TransactionType service = types.get("Service");
		if (service != null) {
			m_service.visit(service, m_days);
		}

		TransactionType call = types.get("Call");
		if (call != null) {
			m_call.visit(call, m_days);
		}

		TransactionType sql = types.get("SQL");
		if (sql != null) {
			m_sql.visit(sql, m_days);
		}
	}

	public static class BaseInfo {
		private long m_avg;

		private long m_errorAvg;

		private double m_errorPercent;

		private long m_errorTotal;

		private double m_responseTime;

		private long m_total;

		public long getAvg() {
			return m_avg;
		}

		public long getErrorAvg() {
			return m_errorAvg;
		}

		public double getErrorPercent() {
			return m_errorPercent;
		}

		public long getErrorTotal() {
			return m_errorTotal;
		}

		public double getResponseTime() {
			return m_responseTime;
		}

		public double getSuccessPercent() {
			if (m_total == 0) {
				return 0;
			}
			return 1 - m_errorPercent;
		}

		public long getTotal() {
			return m_total;
		}

		public void visit(TransactionType type, int days) {
			m_responseTime = type.getAvg();
			m_total = type.getTotalCount();
			m_avg = m_total / days;
			m_errorTotal = type.getFailCount();
			m_errorAvg = m_errorTotal / days;
			if (m_total > 0) {
				m_errorPercent = (double) m_errorTotal / (double) m_total;
			}
		}
	}

	public static class CacheInfo {
		private long m_avg;

		private double m_hitPercent;

		private double m_responseTime;

		private long m_total;

		public long getAvg() {
			return m_avg;
		}

		public double getHitPercent() {
			return m_hitPercent;
		}

		public double getResponseTime() {
			return m_responseTime;
		}

		public long getTotal() {
			return m_total;
		}
	}

	public static class ProblemInfo {
		private long m_avgExceptions;

		private long m_avgLongSqls;

		private long m_avgLongUrls;

		private long m_exceptions;

		private double m_longSqlPercent;

		private long m_longSqls;

		private double m_longUrlPercent;

		private long m_longUrls;

		public long getAvgExceptions() {
			return m_avgExceptions;
		}

		public long getAvgLongSqls() {
			return m_avgLongSqls;
		}

		public long getAvgLongUrls() {
			return m_avgLongUrls;
		}

		public long getExceptions() {
			return m_exceptions;
		}

		public double getLongSqlPercent() {
			return m_longSqlPercent;
		}

		public long getLongSqls() {
			return m_longSqls;
		}

		public double getLongUrlPercent() {
			return m_longUrlPercent;
		}

		public long getLongUrls() {
			return m_longUrls;
		}

		public void setLongSqlPercent(long longSqls) {
			if (longSqls > 0) {
				m_longSqlPercent = (double) m_longSqls / (double) longSqls;
			}
		}

		public void setLongUrlPercent(long longUrls) {
			if (longUrls > 0) {
				m_longUrlPercent = (double) m_longUrls / (double) longUrls;
			}
		}

		public void visit(ProblemStatistics problemStatistics, int days) {
			Map<String, TypeStatistics> status = problemStatistics.getStatus();
			TypeStatistics exceptions = status.get("error");
			if (exceptions != null) {
				m_exceptions = exceptions.getCount();
				m_avgExceptions = m_exceptions / days;
			}

			TypeStatistics longUrl = status.get("long-url");
			if (longUrl != null) {
				m_longUrls = longUrl.getCount();
				m_avgLongUrls = m_longUrls / days;
			}

			TypeStatistics longSql = status.get("long-sql");
			if (longSql != null) {
				m_longSqls = longSql.getCount();
				m_avgLongSqls = m_longSqls / days;
			}
		}
	}
}
