package com.dianping.cat.consumer.core;

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
import com.dainping.cat.consumer.core.dal.Task;
import com.dainping.cat.consumer.core.dal.TaskDao;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.consumer.AbstractMessageAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.status.model.entity.DiskInfo;
import com.dianping.cat.status.model.entity.DiskVolumeInfo;
import com.dianping.cat.status.model.entity.GcInfo;
import com.dianping.cat.status.model.entity.MemoryInfo;
import com.dianping.cat.status.model.entity.MessageInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadsInfo;
import com.dianping.cat.storage.Bucket;
import com.dianping.cat.storage.BucketManager;

public class HeartbeatAnalyzer extends AbstractMessageAnalyzer<HeartbeatReport> implements LogEnabled {
	public static final String ID = "heartbeat";

	@Inject
	private BucketManager m_bucketManager;

	@Inject
	private ReportDao m_reportDao;

	@Inject
	private TaskDao m_taskDao;

	private Map<String, HeartbeatReport> m_reports = new HashMap<String, HeartbeatReport>();

	@Override
	public void doCheckpoint(boolean atEnd) {
		storeReports(atEnd);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
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

	private Period getHeartBeatInfo(Heartbeat heartbeat, long timestamp) {
		String xml = (String) heartbeat.getData();
		StatusInfo info = null;

		try {
			info = com.dianping.cat.status.model.transform.DefaultSaxParser.parse(xml);
		} catch (Exception e) {
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
		HeartbeatReport report = m_reports.get(domain);

		if (report == null) {
			report = new HeartbeatReport(domain);

			report.setStartTime(new Date(m_startTime));
			report.setEndTime(new Date(m_startTime + MINUTE * 60 - 1));
		}
		report.getDomainNames().addAll(m_reports.keySet());

		return report;
	}

	@Override
	protected void loadReports() {
		Bucket<String> reportBucket = null;

		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "heartbeat");

			for (String id : reportBucket.getIds()) {
				String xml = reportBucket.findById(id);
				HeartbeatReport report = DefaultSaxParser.parse(xml);

				m_reports.put(report.getDomain(), report);
			}
		} catch (Exception e) {
			m_logger.error(String.format("Error when loading heartbeat reports of %s!", new Date(m_startTime)), e);
		} finally {
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
				count += processHeartbeat(report, (Heartbeat) message, tree);
			}
		}

		return count;
	}

	private void storeReports(boolean atEnd) {
		DefaultXmlBuilder builder = new DefaultXmlBuilder(true);
		Bucket<String> reportBucket = null;
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());

		t.setStatus(Message.SUCCESS);
		try {
			reportBucket = m_bucketManager.getReportBucket(m_startTime, "heartbeat");

			for (HeartbeatReport report : m_reports.values()) {
				try {
					Set<String> domainNames = report.getDomainNames();
					domainNames.clear();
					domainNames.addAll(m_reports.keySet());

					String xml = builder.buildXml(report);
					String domain = report.getDomain();

					reportBucket.storeById(domain, xml);
				} catch (Exception e) {
					Cat.logError(e);
					t.setStatus(e);
				}
			}

			if (atEnd && !isLocalMode()) {
				Date period = new Date(m_startTime);
				String ip = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();

				for (HeartbeatReport report : m_reports.values()) {
					try {
						Report r = m_reportDao.createLocal();
						String xml = builder.buildXml(report);
						String domain = report.getDomain();

						r.setName("heartbeat");
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
						task.setReportName("heartbeat");
						task.setReportPeriod(period);
						task.setStatus(1); // status todo
						m_taskDao.insert(task);
					} catch (Throwable e) {
						Cat.getProducer().logError(e);
						t.setStatus(e);
					}
				}
			}

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
