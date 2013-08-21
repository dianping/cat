package com.dianping.cat.consumer.top;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportConstants;
import com.dianping.cat.service.ReportManager;

public class TopAnalyzer extends AbstractMessageAnalyzer<TopReport> implements LogEnabled {
	public static final String ID = "top";

	@Inject(ID)
	private ReportManager<TopReport> m_reportManager;

	private TransactionAnalyzer m_transactionAnalyzer;

	private ProblemAnalyzer m_problemAnalyzer;

	@Override
	public void doCheckpoint(boolean atEnd) {
		long startTime = getStartTime();

		if (atEnd && !isLocalMode()) {
			m_reportManager.getHourlyReport(startTime, ReportConstants.CAT, true);
			m_reportManager.getHourlyReports(startTime).put(ReportConstants.CAT, getReport(ReportConstants.CAT));
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(startTime, StoragePolicy.FILE);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public synchronized TopReport getReport(String domain) {
		// TODO to be fixed
		Set<String> domains = m_transactionAnalyzer.getDomains();
		TopReport topReport = new TopReport("Cat");

		topReport.setStartTime(new Date(m_startTime));
		topReport.setEndTime(new Date(m_startTime + 60 * MINUTE - 1));
		for (String domainName : domains) {
			if (validate(domainName)) {
				TransactionReport report = m_transactionAnalyzer.getReport(domainName);

				new TransactionReportVisitor(topReport).visitTransactionReport(report);
			}
		}
		for (String domainName : domains) {
			if (validate(domainName)) {
				ProblemReport report = m_problemAnalyzer.getReport(domainName);

				new ProblemReportVisitor(topReport).visitProblemReport(report);
			}
		}
		return topReport;
	}

	@Override
	protected void process(MessageTree tree) {
		// do nothing
	}

	public void setProblemAnalyzer(ProblemAnalyzer problemAnalyzer) {
		m_problemAnalyzer = problemAnalyzer;
	}

	public void setTransactionAnalyzer(TransactionAnalyzer transactionAnalyzer) {
		m_transactionAnalyzer = transactionAnalyzer;
	}

	public static class ProblemReportVisitor extends com.dianping.cat.consumer.problem.model.transform.BaseVisitor {
		private String m_domain;

		private String m_type;

		private String m_state;

		private TopReport m_report;

		public ProblemReportVisitor(TopReport report) {
			m_report = report;
		}

		@Override
		public void visitEntry(Entry entry) {
			m_type = entry.getType();
			m_state = entry.getStatus();
			super.visitEntry(entry);
		}

		@Override
		public void visitProblemReport(ProblemReport problemReport) {
			m_domain = problemReport.getDomain();
			super.visitProblemReport(problemReport);
		}

		@Override
		public void visitSegment(Segment segment) {
			int id = segment.getId();
			int count = segment.getCount();

			if ("error".equals(m_type)) {
				com.dianping.cat.consumer.top.model.entity.Segment temp = m_report.findOrCreateDomain(m_domain)
				      .findOrCreateSegment(id);
				temp.setError(temp.getError() + count);

				Error error = temp.findOrCreateError(m_state);
				error.setCount(error.getCount() + count);
			}
		}
	}

	public static enum Range2Function {
		URL {
			@Override
			public void apply(Range2 range2, com.dianping.cat.consumer.top.model.entity.Segment detail) {
				long count = range2.getCount();
				long errorCount = range2.getFails();
				double sum = range2.getSum();

				detail.setUrl(count + detail.getUrl());
				detail.setUrlSum(sum + detail.getUrlSum());
				detail.setUrlError(errorCount + detail.getUrlError());
				detail.setUrlDuration(detail.getUrlSum() / detail.getUrl());
			}
		},

		Service {
			@Override
			public void apply(Range2 range2, com.dianping.cat.consumer.top.model.entity.Segment detail) {
				long count = range2.getCount();
				long errorCount = range2.getFails();
				double sum = range2.getSum();

				detail.setService(count + detail.getService());
				detail.setServiceSum(sum + detail.getServiceSum());
				detail.setServiceError(errorCount + detail.getServiceError());
				detail.setServiceDuration(detail.getServiceSum() / detail.getService());
			}
		},

		PigeonService {
			@Override
			public void apply(Range2 range2, com.dianping.cat.consumer.top.model.entity.Segment detail) {
				long count = range2.getCount();
				long errorCount = range2.getFails();
				double sum = range2.getSum();

				detail.setService(count + detail.getService());
				detail.setServiceError(errorCount + detail.getServiceError());
				detail.setServiceSum(sum + detail.getServiceSum());
				detail.setServiceDuration(detail.getServiceSum() / detail.getService());
			}
		},

		Call {
			@Override
			public void apply(Range2 range2, com.dianping.cat.consumer.top.model.entity.Segment detail) {
				long count = range2.getCount();
				long errorCount = range2.getFails();
				double sum = range2.getSum();

				detail.setCall(count + detail.getCall());
				detail.setCallError(errorCount + detail.getCallError());
				detail.setCallSum(sum + detail.getCallSum());
				detail.setCallDuration(detail.getCallSum() / detail.getCall());

			}
		},

		PigeonCall {
			@Override
			public void apply(Range2 range2, com.dianping.cat.consumer.top.model.entity.Segment detail) {
				long count = range2.getCount();
				long errorCount = range2.getFails();
				double sum = range2.getSum();

				detail.setCall(count + detail.getCall());
				detail.setCallError(errorCount + detail.getCallError());
				detail.setCallSum(sum + detail.getCallSum());
				detail.setCallDuration(detail.getCallSum() / detail.getCall());
			}
		},

		SQL {
			@Override
			public void apply(Range2 range2, com.dianping.cat.consumer.top.model.entity.Segment detail) {
				long count = range2.getCount();
				long errorCount = range2.getFails();
				double sum = range2.getSum();

				detail.setSql(count + detail.getSql());
				detail.setSqlError(errorCount + detail.getSqlError());
				detail.setSqlSum(sum + detail.getSqlSum());
				detail.setSqlDuration(detail.getSqlSum() / detail.getSql());
			}
		},
		;

		private static Map<String, Range2Function> s_map = new HashMap<String, Range2Function>();

		static {
			for (Range2Function f : values()) {
				s_map.put(f.name(), f);
			}
		}

		public static Range2Function getByName(String name) {
			return s_map.get(name);
		}

		public abstract void apply(Range2 range2, com.dianping.cat.consumer.top.model.entity.Segment detail);
	}

	static class TransactionReportVisitor extends com.dianping.cat.consumer.transaction.model.transform.BaseVisitor {

		private String m_domain;

		private String m_type;

		private TopReport m_report;

		public TransactionReportVisitor(TopReport report) {
			m_report = report;
		}

		@Override
		public void visitRange2(Range2 range2) {
			int minute = range2.getValue();
			long count = range2.getCount();
			double sum = range2.getSum();
			com.dianping.cat.consumer.top.model.entity.Segment detail = m_report.findOrCreateDomain(m_domain)
			      .findOrCreateSegment(minute);
			Range2Function function = Range2Function.getByName(m_type);

			if (function != null) {
				function.apply(range2, detail);
			} else if (m_type.startsWith("Cache.memcached")) {
				detail.setCache(count + detail.getCache());
				detail.setCacheSum(sum + detail.getCacheSum());
				detail.setCacheDuration(detail.getCacheSum() / detail.getCache());
			}
		}

		@Override
		public void visitTransactionReport(TransactionReport transactionReport) {
			m_domain = transactionReport.getDomain();
			super.visitTransactionReport(transactionReport);
		}

		@Override
		public void visitType(TransactionType type) {
			m_type = type.getId();
			super.visitType(type);
		}
	}

}