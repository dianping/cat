package com.dianping.cat.consumer.heartbeat;

import java.util.Calendar;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;
import com.dianping.cat.status.model.entity.Detail;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.StatusInfo;

public class HeartbeatAnalyzer extends AbstractMessageAnalyzer<HeartbeatReport> implements LogEnabled {
	public static final String ID = "heartbeat";

	@Inject(ID)
	private ReportManager<HeartbeatReport> m_reportManager;

	private com.dianping.cat.consumer.heartbeat.model.entity.Extension convertExtension(Extension extension) {
		com.dianping.cat.consumer.heartbeat.model.entity.Extension he = new com.dianping.cat.consumer.heartbeat.model.entity.Extension();

		he.setId(extension.getId());
		for (Detail detail : extension.getDetails().values()) {
			com.dianping.cat.consumer.heartbeat.model.entity.Detail hd = new com.dianping.cat.consumer.heartbeat.model.entity.Detail(
			      detail.getId()).setValue(detail.getValue());

			he.addDetail(hd);
		}
		return he;
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private HeartbeatReport findOrCreateReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, true);
	}

	private Period buildHeartBeatInfo(Machine machine, Heartbeat heartbeat, long timestamp) {
		String xml = (String) heartbeat.getData();
		StatusInfo info = null;

		try {
			info = com.dianping.cat.status.model.transform.DefaultSaxParser.parse(xml);
		} catch (Exception e) {
			m_logger.error(xml);
			m_logger.error(e.getMessage(), e);
			return null;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int minute = cal.get(Calendar.MINUTE);
		Period period = new Period(minute);

		for (Extension extension : info.getExtensions().values()) {
			period.addExtension(convertExtension(extension));
		}
		return period;
	}

	@Override
	public void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private HeartbeatReport findOrCreateReport(String domain) {
		return m_reportManager.getHourlyReport(getStartTime(), domain, true);
	}

	@Override
	public HeartbeatReport getReport(String domain) {
		HeartbeatReport report = m_reportManager.getHourlyReport(getStartTime(), domain, false);

		report.getDomainNames().addAll(m_reportManager.getDomains(getStartTime()));
		return report;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE);
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();
		Message message = tree.getMessage();
		HeartbeatReport report = findOrCreateReport(domain);
		report.addIp(tree.getIpAddress());

		if (message instanceof Transaction) {
			processTransaction(report, tree, (Transaction) message);
		}
	}

	private void processHeartbeat(HeartbeatReport report, Heartbeat heartbeat, MessageTree tree) {
		String ip = tree.getIpAddress();
		Machine machine = report.findOrCreateMachine(ip);
		Period period = buildHeartBeatInfo(machine, heartbeat, tree.getMessage().getTimestamp());

		if (period != null) {
			machine.getPeriods().add(period);
		}
	}

	private void processTransaction(HeartbeatReport report, MessageTree tree, Transaction transaction) {
		List<Message> children = transaction.getChildren();

		for (Message message : children) {
			if (message instanceof Transaction) {
				Transaction temp = (Transaction) message;

				processTransaction(report, tree, temp);
			} else if (message instanceof Heartbeat) {
				if (message.getType().equalsIgnoreCase("heartbeat")) {
					processHeartbeat(report, (Heartbeat) message, tree);
				}
			}
		}
	}

}
