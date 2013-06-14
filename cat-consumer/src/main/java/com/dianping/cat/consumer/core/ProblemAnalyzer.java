package com.dianping.cat.consumer.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.core.dal.Report;
import com.dianping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.consumer.core.dal.Task;
import com.dianping.cat.consumer.core.dal.TaskDao;
import com.dianping.cat.consumer.core.problem.ProblemHandler;
import com.dianping.cat.consumer.core.problem.ProblemReportAggregation;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.problem.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class ProblemAnalyzer extends AbstractMessageAnalyzer<ProblemReport> implements LogEnabled, Initializable {
	public static final String ID = "problem";

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	@Inject
	private List<ProblemHandler> m_handlers;

	@Inject
	private ProblemReportAggregation m_problemReportAggregation;

	private static final String FRONT_END = "FrontEnd";

	private Map<String, ProblemReport> m_reports = new HashMap<String, ProblemReport>();

	private ProblemReport buildFrontEndReport(ProblemReport report) {
		m_problemReportAggregation.refreshRule();
		report.accept(m_problemReportAggregation);

		return m_problemReportAggregation.getReport();
	}

	private ProblemReport buildTotalProblemReport() {
		ProblemReport report = new ProblemReport(ALL);
		ProblemReportAllBuilder visitor = new ProblemReportAllBuilder(report);

		try {
			for (ProblemReport temp : m_reports.values()) {
				report.getIps().add(temp.getDomain());
				report.getDomainNames().add(temp.getDomain());
				visitor.visitProblemReport(temp);
			}
		} catch (Exception e) {
			Cat.logError(e);
		}
		return report;
	}

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
		return m_reports.keySet();
	}

	@Override
	public ProblemReport getReport(String domain) {
		if (!ALL.equals(domain)) {
			ProblemReport report = m_reports.get(domain);

			if (report == null) {
				report = new ProblemReport(domain);

				report.setStartTime(new Date(m_startTime));
				report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
			}
			report.getDomainNames().addAll(m_reports.keySet());
			
			
			return report;
		} else {
			return buildTotalProblemReport();
		}
	}

	@Override
	public void initialize() throws InitializationException {
		// to work around a performance issue within plexus
		m_handlers = new ArrayList<ProblemHandler>(m_handlers);
	}

	protected void loadReports() {
		Bucket<String> bucket = null;

		try {
			bucket = m_bucketManager.getReportBucket(m_startTime, "problem");

			for (String id : bucket.getIds()) {
				String xml = bucket.findById(id);
				ProblemReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading problem reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (bucket != null) {
				m_bucketManager.closeBucket(bucket);
			}
		}
	}

	@Override
	public void process(MessageTree tree) {
		String domain = tree.getDomain();
		ProblemReport report = m_reports.get(domain);

		if (report == null) {
			report = new ProblemReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}

		report.addIp(tree.getIpAddress());
		Machine machine = report.findOrCreateMachine(tree.getIpAddress());

		for (ProblemHandler handler : m_handlers) {
			handler.handle(machine, tree);
		}
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "problem");

			for (ProblemReport report : m_reports.values()) {
				try {
					Set<String> domainNames = report.getDomainNames();
					domainNames.clear();
					domainNames.addAll(getDomains());

					String xml = builder.buildXml(report);
					String domain = report.getDomain();

					reportBucket.storeById(domain, xml);
				} catch (Exception e) {
					t.setStatus(e);
					Cat.getProducer().logError(e);
				}
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				ProblemReport frontEnd = m_reports.get(FRONT_END);

				if (frontEnd != null) {
					m_reports.put(FRONT_END, buildFrontEndReport(frontEnd));
				}

				ProblemReport all = buildTotalProblemReport();

				m_reports.put(ALL, all);

				for (ProblemReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = builder.buildXml(report);
						String domain = report.getDomain();

						r.setName(ID);
						r.setDomain(domain);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(1);
						r.setContent(xml);

						m_reportDao.insert(r);

						Task task = m_taskDao.createLocal();
						task.setCreationDate(new Date());
						task.setProducer(ip);
						task.setReportDomain(domain);
						task.setReportName(ID);
						task.setReportPeriod(period);
						task.setStatus(1);
						m_taskDao.insert(task);
					} catch (Throwable e) {
						t.setStatus(e);
						Cat.getProducer().logError(e);
					}
				}
			}
		} catch (Exception e) {
			t.setStatus(e);
			Cat.getProducer().logError(e);
			m_logger.error(String.format("Error when storing problem reports to %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

}
