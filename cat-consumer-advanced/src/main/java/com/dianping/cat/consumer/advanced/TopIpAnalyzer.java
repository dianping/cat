package com.dianping.cat.consumer.advanced;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.core.dal.Report;
import com.dainping.cat.consumer.core.dal.ReportDao;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.AbstractMessageAnalyzer;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.ip.model.entity.Ip;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.entity.Period;
import com.dianping.cat.consumer.ip.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.ip.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class TopIpAnalyzer extends AbstractMessageAnalyzer<IpReport> implements LogEnabled {
	public static final String ID = "ip";
	
	private static final String TOKEN = "RemoteIP=";

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	private Map<String, IpReport> m_reports = new HashMap<String, IpReport>();

	private int m_lastPhase;

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
	public IpReport getReport(String domain) {
		IpReport report = m_reports.get(domain);

		if (report == null) {
			report = new IpReport(domain);
		}
		report.getDomainNames().clear();
		report.getDomainNames().addAll(m_reports.keySet());

		return report;
	}

	@Override
	protected void loadReports() {
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "ip");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				IpReport report = DefaultSaxParser.parse(xml);

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

	private void clearLastPhase() {
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);
		int currentPhase = minute / 20; // 0, 1, 2

		if (m_lastPhase != currentPhase) {
			int baseIndex = m_lastPhase * 20;
			List<String> domains = new ArrayList<String>();

			for (Map.Entry<String, IpReport> e : m_reports.entrySet()) {
				IpReport report = e.getValue();
				Map<Integer, Period> periods = report.getPeriods();

				for (int i = 0; i < 20; i++) {
					periods.remove(baseIndex + i);
				}

				if (periods.isEmpty()) {
					domains.add(e.getKey());
				}
			}

			for (String domain : domains) {
				m_reports.remove(domain);
			}

			m_lastPhase = currentPhase;
		}
	}

	private String getIpAddress(Transaction root) {
		List<Message> children = root.getChildren();

		for (Message child : children) {
			if (child instanceof Event && child.getType().equals("URL") && child.getName().equals("ClientInfo")) {
				// URL:ClientInfo RemoteIP=<ip>&...
				String data = child.getData().toString();
				int off = data.indexOf(TOKEN);

				if (off >= 0) {
					int length = TOKEN.length();
					int pos = data.indexOf('&', off + length);

					if (pos > 0) {
						return data.substring(off + length, pos);
					}
				}
				break;
			} else if (child instanceof Heartbeat) {
				// Heartbeat:<ip>
				return child.getName();
			}
		}

		return null;
	}

	@Override
	protected void process(MessageTree tree) {
		Message root = tree.getMessage();

		if (root instanceof Transaction) {
			String address = getIpAddress((Transaction) root);

			if (address == null) {
				address = "N/A";

				m_logger.debug("Unable to find IP address from message: " + tree);
			}

			String domain = tree.getDomain();
			Calendar cal = Calendar.getInstance();

			cal.setTimeInMillis(root.getTimestamp());

			int minute = cal.get(Calendar.MINUTE);
			IpReport report = findOrCreateReport(domain);
			Period period = report.findOrCreatePeriod(minute);
			Ip ip = period.findOrCreateIp(address);

			ip.incCount();

			clearLastPhase();
		}
	}

	private IpReport findOrCreateReport(String domain) {
		IpReport report = m_reports.get(domain);

		if (report == null) {
			synchronized (m_reports) {
				report = m_reports.get(domain);

				if (report == null) {
					report = new IpReport(domain);
					m_reports.put(domain, report);
				}
			}
		}

		return report;
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "ip");

			for (IpReport report : m_reports.values()) {
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

				for (IpReport report : m_reports.values()) {
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
}
