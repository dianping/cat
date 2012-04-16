package com.dianping.cat.consumer.heartbeat;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultXmlBuilder;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultXmlParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadInfo;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class HeartbeatAnalyzer extends AbstractMessageAnalyzer<HeartbeatReport> implements LogEnabled {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	private Map<String, HeartbeatReport> m_reports = new HashMap<String, HeartbeatReport>();

	private long m_extraTime;

	private long m_startTime;

	private long m_duration;

	private Logger m_logger;

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

	public HeartbeatReport getReport(String domain) {
		HeartbeatReport report = m_reports.get(domain);

		if (report == null) {
			report = new HeartbeatReport(domain);
		}
		report.getDomainNames().clear();
		report.getDomainNames().addAll(m_reports.keySet());

		return report;
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	private void loadReports() {
		DefaultXmlParser parser = new DefaultXmlParser();
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "ip");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				HeartbeatReport report = parser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading ip reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	private HeartbeatReport findOrCreateReport(String domain) {
		HeartbeatReport report = m_reports.get(domain);

		if (report == null) {
			synchronized (m_reports) {
				report = m_reports.get(domain);

				if (report == null) {
					report = new HeartbeatReport(domain);
					report.setStartTime(new Date(m_startTime));
					report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
					m_reports.put(domain, report);
				}
			}
		}

		return report;
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

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "ip");

			for (HeartbeatReport report : m_reports.values()) {
				Set<String> domainNames = report.getDomainNames();
				domainNames.clear();
				domainNames.addAll(getDomains());

				String xml = builder.buildXml(report);
				String domain = report.getDomain();

				reportBucket.storeById(domain, xml);
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				for (HeartbeatReport report : m_reports.values()) {
					Report r = m_reportDao.createLocal();
					String xml = builder.buildXml(report);
					String domain = report.getDomain();

					r.setName("ip");
					r.setDomain(domain);
					r.setPeriod(period);
					r.setIp(ip);
					r.setType(1);
					r.setContent(xml);

					m_reportDao.insert(r);
				}
			}

			t.setStatus(Message.SUCCESS);
		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing ip reports of %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		Message message = tree.getMessage();
		HeartbeatReport report = findOrCreateReport(domain);

		if (message instanceof Transaction) {
			int count = processTransaction(report, tree, (Transaction) message);
			if (count > 0) {
				storeMessage(tree);
			}
		}
	}

	private void setHeartBeatInfo(Period period, Heartbeat heartbeat) {
		String xml = (String) heartbeat.getData();
		try {
			StatusInfo info = new com.dianping.cat.status.model.transform.DefaultXmlParser().parse(xml);
			ThreadInfo thread = info.getThread();
			period.setThreadCount(thread.getCount());
			period.setDaemonCount(thread.getDaemonCount());
			period.setTotalStartedCount((int) thread.getTotalStartedCount());
		} catch (Exception e) {
			period.setThreadCount(-1);
			period.setDaemonCount(-1);
			period.setTotalStartedCount(-1);
		}
	}

	private int processHeartbeat(HeartbeatReport report, Heartbeat heartbeat, MessageTree tree) {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(heartbeat.getTimestamp());
		int minute = cal.get(Calendar.MINUTE);

		Period period = new Period(minute);
		setHeartBeatInfo(period, heartbeat);
		report.getPeriods().add(period);
		return 1;
	}

	private int processTransaction(HeartbeatReport report, MessageTree tree, Transaction transaction) {
		List<Message> children = transaction.getChildren();
		int count = 0;

		for (Message message : children) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				count += processTransaction(report, tree, temp);
			} else if (message instanceof Heartbeat) {
				count += processHeartbeat(report, (Heartbeat) message, tree);
			}
		}

		return count;
	}

	private void storeMessage(MessageTree tree) {
		String messageId = tree.getMessageId();
		String domain = tree.getDomain();

		try {
			Bucket<MessageTree> logviewBucket = m_bucketManager.getLogviewBucket(m_startTime, domain);

			logviewBucket.storeById(messageId, tree);
		} catch (IOException e) {
			m_logger.error("Error when storing logview for transaction analyzer!", e);
		}
	}
}
