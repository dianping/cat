package com.dianping.cat.consumer.cross;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dainping.cat.consumer.dal.report.Report;
import com.dainping.cat.consumer.dal.report.ReportDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.cross.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;
import com.site.lookup.annotation.Inject;

public class CrossAnalyzer extends AbstractMessageAnalyzer<CrossReport> implements LogEnabled {
	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	private Map<String, CrossReport> m_reports = new HashMap<String, CrossReport>();

	private static final String UNKNOWN = "UnknownIp";

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

	private String getIpFromMessageId(String messageId) {
		MessageId id = MessageId.parse(messageId);

		return id.getIpAddress();
	}

	public CrossReport getReport(String domain) {
		CrossReport report = m_reports.get(domain);

		if (report == null) {
			report = new CrossReport(domain);
		}

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
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "cross");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				CrossReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading cross reports of %s!", new Date(m_startTime)), e);
		} finally {
			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	public CrossInfo parseCorssTransaction(Transaction t, MessageTree tree) {
		if (shouldDiscard(t)) {
			return null;
		}

		String type = t.getType();

		if ("PigeonCall".equals(type) || "Call".equals(type)) {
			return parsePigeonClientTransaction(t, tree);
		} else if ("PigeonService".equals(type) || "Service".equals(type)) {
			return parsePigeonServerTransaction(t, tree);
		}
		return null;
	}

	private CrossInfo parsePigeonClientTransaction(Transaction t, MessageTree tree) {
		CrossInfo crossInfo = new CrossInfo();
		String localIp = tree.getIpAddress();
		List<Message> messages = t.getChildren();

		for (Message message : messages) {
			if (message instanceof Event) {
				if (message.getType().equals("PigeonCall.server")) {
					crossInfo.setRemoteAddress(message.getName());
					break;
				}
			}
		}

		crossInfo.setLocalAddress(localIp);
		crossInfo.setRemoteRole("Pigeon.Server");
		crossInfo.setDetailType("PigeonCall");
		return crossInfo;
	}

	private CrossInfo parsePigeonServerTransaction(Transaction t, MessageTree tree) {
		CrossInfo crossInfo = new CrossInfo();
		String localIp = tree.getIpAddress();

		List<Message> messages = t.getChildren();
		for (Message message : messages) {
			if (message instanceof Event) {
				if (message.getType().equals("PigeonService.client")) {
					String name = message.getName();
					int index = name.indexOf(":");
					if (index > 0) {
						name = name.substring(0, index);
					}
					crossInfo.setRemoteAddress(name);
					break;
				}
			}
		}
		if (crossInfo.getRemoteAddress().equals(UNKNOWN)) {
			String remoteIp = getIpFromMessageId(tree.getMessageId());
			crossInfo.setRemoteAddress(remoteIp);
		}
		crossInfo.setLocalAddress(localIp);
		crossInfo.setRemoteRole("Pigeon.Client");
		crossInfo.setDetailType("PigeonService");
		return crossInfo;
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		CrossReport report = m_reports.get(domain);

		if (report == null) {
			report = new CrossReport(domain);
			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));

			m_reports.put(domain, report);
		}

		Message message = tree.getMessage();
		report.addIp(tree.getIpAddress());

		if (message instanceof Transaction) {
			processTransaction(report, tree, (Transaction) message);
		}
	}

	private void processTransaction(CrossReport report, MessageTree tree, Transaction t) {
		CrossInfo info = parseCorssTransaction(t, tree);
		if (info != null) {
			updateCrossReport(report, t, info);
		}

		List<Message> children = t.getChildren();

		for (Message child : children) {
			if (child instanceof Transaction) {
				processTransaction(report, tree, (Transaction) child);
			}
		}
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
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "cross");

			for (CrossReport report : m_reports.values()) {
				try {
					Set<String> domainNames = report.getDomainNames();
					domainNames.clear();
					domainNames.addAll(getDomains());

					String xml = builder.buildXml(report);
					String domain = report.getDomain();

					reportBucket.storeById(domain, xml);
				} catch (Exception e) {
					Cat.getProducer().logError(e);
					t.setStatus(e);
				}
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				for (CrossReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = builder.buildXml(report);
						String domain = report.getDomain();

						r.setName("cross");
						r.setDomain(domain);
						r.setPeriod(period);
						r.setIp(ip);
						r.setType(1);
						r.setContent(xml);

						m_reportDao.insert(r);

					} catch (Throwable e) {
						Cat.getProducer().logError(e);
						t.setStatus(e);
					}
				}
			}

		} catch (Exception e) {
			Cat.getProducer().logError(e);
			t.setStatus(e);
			m_logger.error(String.format("Error when storing cross reports of %s!", new Date(m_startTime)), e);
		} finally {
			t.complete();

			if (reportBucket != null) {
				m_bucketManager.closeBucket(reportBucket);
			}
		}
	}

	private void updateCrossReport(CrossReport report, Transaction t, CrossInfo info) {
		String localIp = info.getLocalAddress();
		String remoteIp = info.getRemoteAddress();
		String role = info.getRemoteRole();
		String transactionName = t.getName();

		Local client = report.findOrCreateLocal(localIp);
		Remote server = client.findOrCreateRemote(remoteIp);

		server.setRole(role);

		Type type = server.getType();

		if (type == null) {
			type = new Type();
			type.setId(info.getDetailType());
			server.setType(type);
		}
		Name name = type.findOrCreateName(transactionName);

		type.incTotalCount();
		name.incTotalCount();

		if (!t.isSuccess()) {
			type.incFailCount();
			name.incFailCount();
		}

		double duration = t.getDurationInMicros() / 1000d;
		name.setSum(name.getSum() + duration);
		type.setSum(type.getSum() + duration);
	}

	static class CrossInfo {
		private String m_remoteRole = UNKNOWN;

		private String m_LocalAddress = UNKNOWN;

		private String m_RemoteAddress = UNKNOWN;

		private String m_detailType = UNKNOWN;

		public String getDetailType() {
			return m_detailType;
		}

		public String getLocalAddress() {
			return m_LocalAddress;
		}

		public String getRemoteAddress() {
			return m_RemoteAddress;
		}

		public String getRemoteRole() {
			return m_remoteRole;
		}

		public void setDetailType(String detailType) {
			m_detailType = detailType;
		}

		public void setLocalAddress(String localAddress) {
			m_LocalAddress = localAddress;
		}

		public void setRemoteAddress(String remoteAddress) {
			m_RemoteAddress = remoteAddress;
		}

		public void setRemoteRole(String remoteRole) {
			m_remoteRole = remoteRole;
		}
	}
}
