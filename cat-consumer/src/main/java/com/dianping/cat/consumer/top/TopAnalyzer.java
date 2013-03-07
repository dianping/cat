package com.dianping.cat.consumer.top;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dainping.cat.consumer.dal.report.TaskDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.top.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Range2;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class TopAnalyzer extends AbstractMessageAnalyzer<TopReport> implements LogEnabled {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	private TopReport m_report;

	private TransactionAnalyzer m_transactionAnalyzer;

	private ProblemAnalyzer m_problemAnalyzer;

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReports(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Set<String> getDomains() {
		return new HashSet<String>();
	}

	public synchronized TopReport getReport(String domain) {
		m_report = new TopReport("Cat");
		m_report.setStartTime(new Date(m_startTime));
		m_report.setEndTime(new Date(m_startTime + 59 * MINUTE));

		Set<String> domains = m_transactionAnalyzer.getDomains();
		for (String temp : domains) {
			TransactionReport report = m_transactionAnalyzer.getReport(temp);

			new TransactionReportVisitor().visitTransactionReport(report);
		}
		for (String temp : domains) {
			ProblemReport report = m_problemAnalyzer.getReport(temp);

			new ProblemReportVisitor().visitProblemReport(report);
		}
		return m_report;
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	private void loadReports() {
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "top");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				TopReport report = DefaultSaxParser.parse(xml);

				m_report = report;
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading top reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	@Override
	protected void process(MessageTree tree) {
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;

		loadReports();
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);

		try {
			if (atEnd && !isLocalMode()) {
				getReport("Cat");
				reportBucket = m_bucketManager.getReportBucket(m_startTime, "top");

				try {
					String xml = builder.buildXml(m_report);
					String domain = m_report.getDomain();

					reportBucket.storeById(domain, xml);
				} catch (Exception e) {
					t.setStatus(e);
					Cat.logError(e);
				}

				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				try {
					Report r = m_reportDao.createLocal();
					String xml = builder.buildXml(m_report);
					String domain = m_report.getDomain();

					r.setName("top");
					r.setDomain(domain);
					r.setPeriod(period);
					r.setIp(ip);
					r.setType(1);
					r.setContent(xml);

					m_reportDao.insert(r);
				} catch (Throwable e) {
					t.setStatus(e);
					Cat.getProducer().logError(e);
				}
			}
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing top reports of %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	public void setTransactionAnalyzer(TransactionAnalyzer transactionAnalyzer) {
		m_transactionAnalyzer = transactionAnalyzer;
	}

	public void setProblemAnalyzer(ProblemAnalyzer problemAnalyzer) {
		m_problemAnalyzer = problemAnalyzer;
	}

	class TransactionReportVisitor extends com.dianping.cat.consumer.transaction.model.transform.BaseVisitor {

		private String m_domain;

		private String m_type;

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

		@Override
		public void visitRange2(Range2 range2) {
			int minute = range2.getValue();
			long count = range2.getCount();
			double sum = range2.getSum();

			com.dianping.cat.consumer.top.model.entity.Segment detail = m_report.findOrCreateDomain(m_domain)
			      .findOrCreateSegment(minute);

			if ("URL".equals(m_type)) {
				detail.setUrl(count + detail.getUrl());
				detail.setUrlSum(sum + detail.getUrlSum());
				detail.setUrlDuration(detail.getUrlSum() / detail.getUrl());
			} else if ("PigeonService".equals(m_type) || "Service".equals(m_type)) {
				detail.setService(count + detail.getService());
				detail.setServiceSum(sum + detail.getServiceSum());
				detail.setServiceDuration(detail.getServiceSum() / detail.getService());
			} else if ("PigeonCall".equals(m_type) || "Call".equals(m_type)) {
				detail.setCall(count + detail.getCall());
				detail.setCallSum(sum + detail.getCallSum());
				detail.setCallDuration(detail.getCallSum() / detail.getCall());
			} else if (m_type.startsWith("Cache.memcached")) {
				detail.setCache(count + detail.getCache());
				detail.setCacheSum(sum + detail.getCacheSum());
				detail.setCacheDuration(detail.getCacheSum() / detail.getCache());
			} else if ("SQL".equals(m_type)) {
				detail.setSql(count + detail.getSql());
				detail.setSqlSum(sum + detail.getSqlSum());
				detail.setSqlDuration(detail.getSqlSum() / detail.getSql());
			}
		}
	}

	class ProblemReportVisitor extends com.dianping.cat.consumer.problem.model.transform.BaseVisitor {
		private String m_domain;

		private String m_type;

		@Override
		public void visitProblemReport(ProblemReport problemReport) {
			m_domain = problemReport.getDomain();
			super.visitProblemReport(problemReport);
		}

		@Override
		public void visitEntry(Entry entry) {
			m_type = entry.getType();
			super.visitEntry(entry);
		}

		@Override
		public void visitSegment(Segment segment) {
			int id = segment.getId();
			int count = segment.getCount();

			if ("error".equals(m_type)) {
				com.dianping.cat.consumer.top.model.entity.Segment temp = m_report.findOrCreateDomain(m_domain)
				      .findOrCreateSegment(id);
				temp.setError(count);
			} else if ("call".equals(m_type)) {
				com.dianping.cat.consumer.top.model.entity.Segment temp = m_report.findOrCreateDomain(m_domain)
				      .findOrCreateSegment(id);
				temp.setCallError(count);
			}
		}
	}

}
