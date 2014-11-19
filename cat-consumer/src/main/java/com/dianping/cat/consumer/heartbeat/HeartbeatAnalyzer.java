package com.dianping.cat.consumer.heartbeat;

import java.util.Calendar;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.Property;
import com.dianping.cat.status.model.entity.StatusInfo;

public class HeartbeatAnalyzer extends AbstractMessageAnalyzer<HeartbeatReport> implements LogEnabled {
	public static final String ID = "heartbeat";

	@Inject(ID)
	private ReportManager<HeartbeatReport> m_reportManager;

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

	private Period getHeartBeatInfo(Heartbeat heartbeat, long timestamp) {
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

		try {
			for (Property property : info.getProperties().values()) {
				if ("GC".equals(property.getId())) {
					List<Extension> gcs = property.getExtensions();

					for (Extension gc : gcs) {
						String name = gc.getDynamicAttribute("Name");

						if ("ParNew".equals(name) || "PS Scavenge".equals(name)) {
							com.dianping.cat.consumer.heartbeat.model.entity.Property hp = new com.dianping.cat.consumer.heartbeat.model.entity.Property(
							      "NewGcCount");

							hp.setValue(gc.getDynamicAttribute("Count"));
							period.addProperty(hp);
						} else if ("ConcurrentMarkSweep".equals(name) || "PS MarkSweep".equals(name)) {
							com.dianping.cat.consumer.heartbeat.model.entity.Property hp = new com.dianping.cat.consumer.heartbeat.model.entity.Property(
							      "NewGcCount");

							hp.setValue(gc.getDynamicAttribute("Count"));
							period.addProperty(hp);
						}
					}
				} else if ("DiskVolume".equals(property.getId())) {
					List<Extension> diskVolumes = property.getExtensions();

					if (diskVolumes != null) {
						com.dianping.cat.consumer.heartbeat.model.entity.Property hp = new com.dianping.cat.consumer.heartbeat.model.entity.Property(
						      "Disk");

						for (Extension volumeInfo : diskVolumes) {
							com.dianping.cat.consumer.heartbeat.model.entity.Extension he = new com.dianping.cat.consumer.heartbeat.model.entity.Extension();

							he.setDynamicAttribute("Name", volumeInfo.getDynamicAttribute("Name"));
							he.setDynamicAttribute("Total", volumeInfo.getDynamicAttribute("Total"));
							he.setDynamicAttribute("Free", volumeInfo.getDynamicAttribute("Free"));
							he.setDynamicAttribute("Usable", volumeInfo.getDynamicAttribute("Usable"));
							hp.addExtension(he);
						}
						period.addProperty(hp);
					}
				} else {
					period.addProperty(convertProperty(property));
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return period;
	}

	private com.dianping.cat.consumer.heartbeat.model.entity.Property convertProperty(Property property) {
		com.dianping.cat.consumer.heartbeat.model.entity.Property hp = new com.dianping.cat.consumer.heartbeat.model.entity.Property();

		hp.setId(property.getId());
		hp.setValue(property.getValue());
		return hp;
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
		Period period = getHeartBeatInfo(heartbeat, tree.getMessage().getTimestamp());

		if (period != null) {
			report.findOrCreateMachine(ip).getPeriods().add(period);
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
