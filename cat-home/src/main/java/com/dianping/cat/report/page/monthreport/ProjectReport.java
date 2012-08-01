package com.dianping.cat.report.page.monthreport;

import java.util.Date;
import java.util.Map;

import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.report.page.problem.ProblemStatistics;
import com.dianping.cat.report.page.problem.ProblemStatistics.TypeStatistics;

public class ProjectReport {

	private String m_domain;

	private Date m_start;

	private Date m_end;

	private int m_days;

	private BaseInfo m_url = new BaseInfo();
	
	private BaseInfo m_service = new BaseInfo();

	private BaseInfo m_sql = new BaseInfo();

	private BaseInfo m_call = new BaseInfo();

	private CacheInfo m_cache = new CacheInfo();

	private ProblemInfo m_problem = new ProblemInfo();

	public CacheInfo getCache() {
		return m_cache;
	}

	public BaseInfo getCall() {
		return m_call;
	}

	public String getDomain() {
		return m_domain;
	}

	public ProblemInfo getProblem() {
		return m_problem;
	}

	public BaseInfo getUrl() {
		return m_url;
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

	public Date getEnd() {
		return m_end;
	}

	public int getDays() {
		return m_days;
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
		private double m_responseTime;

		private long m_total;

		private long m_avg;

		private long m_errorTotal;

		private long m_errorAvg;

		private double m_errorPercent;

		public long getAvg() {
			return m_avg;
		}

		public long getErrorAvg() {
			return m_errorAvg;
		}

		public double getErrorPercent() {
			return m_errorPercent;
		}

		public double getSuccessPercent() {
			if(m_total ==0){
				return 0;
			}
			return 1 - m_errorPercent;
		}

		public long getErrorTotal() {
			return m_errorTotal;
		}

		public double getResponseTime() {
			return m_responseTime;
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
		private double m_responseTime;

		private double m_hitPercent;

		private long m_total;

		private long m_avg;

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
		private long m_exceptions;

		private long m_avgExceptions;

		private long m_longSqls;

		private long m_avgLongSqls;

		private double m_longSqlPercent;

		private long m_longUrls;

		private long m_avgLongUrls;

		private double m_longUrlPercent;

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

		public long getLongSqls() {
			return m_longSqls;
		}

		public long getLongUrls() {
			return m_longUrls;
		}

		public double getLongSqlPercent() {
			return m_longSqlPercent;
		}

		public void setLongSqlPercent(long longSqls) {
			if (longSqls > 0) {
				m_longSqlPercent = m_longSqls / longSqls;
			}
		}

		public double getLongUrlPercent() {
			return m_longUrlPercent;
		}

		public void setLongUrlPercent(long longUrls) {
			if (longUrls > 0) {
				m_longUrlPercent = m_longUrls / longUrls;
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
