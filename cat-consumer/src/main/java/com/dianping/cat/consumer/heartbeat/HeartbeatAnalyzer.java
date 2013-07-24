package com.dianping.cat.consumer.heartbeat;

import java.util.Calendar;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.service.DefaultReportManager.StoragePolicy;
import com.dianping.cat.service.ReportManager;
import com.dianping.cat.status.model.entity.DiskInfo;
import com.dianping.cat.status.model.entity.DiskVolumeInfo;
import com.dianping.cat.status.model.entity.GcInfo;
import com.dianping.cat.status.model.entity.MemoryInfo;
import com.dianping.cat.status.model.entity.MessageInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadsInfo;

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
			System.out.println(xml);
			m_logger.error("Error when parse status info in heartbeat analyzer." + xml, e);

			Cat.getProducer().logEvent("HearbeatAnalyzer", "ErrorXml", "Error", xml);
			return null;
		}

		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(timestamp);
		int minute = cal.get(Calendar.MINUTE);
		Period period = new Period(minute);

		try {
			ThreadsInfo thread = info.getThread();

			period.setThreadCount(thread.getCount());
			period.setDaemonCount(thread.getDaemonCount());
			period.setTotalStartedCount(thread.getTotalStartedCount());
			period.setCatThreadCount(thread.getCatThreadCount());
			period.setPigeonThreadCount(thread.getPigeonThreadCount());
			period.setHttpThreadCount(thread.getHttpThreadCount());

			MessageInfo catInfo = info.getMessage();

			period.setCatMessageProduced(catInfo.getProduced());
			period.setCatMessageOverflow(catInfo.getOverflowed());
			period.setCatMessageSize(catInfo.getBytes());

			MemoryInfo memeryInfo = info.getMemory();
			List<GcInfo> gcs = info.getMemory().getGcs();

			for (GcInfo gc : gcs) {
				String name = gc.getName();

				if ("ParNew".equals(name) || "PS Scavenge".equals(name)) {
					period.setNewGcCount(gc.getCount());
				} else if ("ConcurrentMarkSweep".equals(name) || "PS MarkSweep".equals(name)) {
					period.setOldGcCount(gc.getCount());
				}
			}

			period.setHeapUsage(memeryInfo.getHeapUsage());
			period.setNoneHeapUsage(memeryInfo.getNonHeapUsage());
			period.setMemoryFree(memeryInfo.getFree());
			period.setSystemLoadAverage(info.getOs().getSystemLoadAverage());

			DiskInfo diskInfo = info.getDisk();

			if (diskInfo != null) {
				for (DiskVolumeInfo volumeInfo : diskInfo.getDiskVolumes()) {
					Disk disk = new Disk(volumeInfo.getId());

					disk.setTotal(volumeInfo.getTotal());
					disk.setFree(volumeInfo.getFree());
					disk.setUsable(volumeInfo.getUsable());
					period.addDisk(disk);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return period;
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

	private int processHeartbeat(HeartbeatReport report, Heartbeat heartbeat, MessageTree tree) {
		String ip = tree.getIpAddress();
		Period period = getHeartBeatInfo(heartbeat, tree.getMessage().getTimestamp());

		if (period != null) {
			report.findOrCreateMachine(ip).getPeriods().add(period);
		}

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
				if (message.getType().equalsIgnoreCase("heartbeat")) {
					count += processHeartbeat(report, (Heartbeat) message, tree);
				}
			}
		}

		return count;
	}

}
