package com.dianping.cat.consumer.heartbeat;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.analysis.AbstractMessageAnalyzer;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.message.Heartbeat;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.report.DefaultReportManager.StoragePolicy;
import com.dianping.cat.status.model.entity.DiskVolumeInfo;
import com.dianping.cat.status.model.entity.Extension;
import com.dianping.cat.status.model.entity.ExtensionDetail;
import com.dianping.cat.status.model.entity.GcInfo;
import com.dianping.cat.status.model.entity.MemoryInfo;
import com.dianping.cat.status.model.entity.MessageInfo;
import com.dianping.cat.status.model.entity.OsInfo;
import com.dianping.cat.status.model.entity.StatusInfo;
import com.dianping.cat.status.model.entity.ThreadsInfo;

public class HeartbeatAnalyzer extends AbstractMessageAnalyzer<HeartbeatReport> implements LogEnabled {
	public static final String ID = "heartbeat";

	@Inject(ID)
	private ReportManager<HeartbeatReport> m_reportManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	private Period buildHeartBeatInfo(Machine machine, Heartbeat heartbeat, long timestamp) {
		String xml = (String) heartbeat.getData();
		StatusInfo info = null;

		try {
			info = com.dianping.cat.status.model.transform.DefaultSaxParser.parse(xml);
			machine.setClasspath(info.getRuntime().getJavaClasspath());

			transalteHearbeat(info);
		} catch (Exception e) {
			return null;
		}

		try {
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(timestamp);
			int minute = cal.get(Calendar.MINUTE);
			Period period = new Period(minute);

			for (Entry<String, Extension> entry : info.getExtensions().entrySet()) {
				String id = entry.getKey();
				Extension ext = entry.getValue();
				com.dianping.cat.consumer.heartbeat.model.entity.Extension extension = period.findOrCreateExtension(id);
				Map<String, ExtensionDetail> details = ext.getDetails();

				for (Entry<String, ExtensionDetail> detail : details.entrySet()) {
					ExtensionDetail extensionDetail = detail.getValue();

					extension.findOrCreateDetail(extensionDetail.getId()).setValue(extensionDetail.getValue());
				}
			}
			return period;
		} catch (Exception e) {
			Cat.logError(e);
			return null;
		}
	}

	@Override
	public synchronized void doCheckpoint(boolean atEnd) {
		if (atEnd && !isLocalMode()) {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE_AND_DB, m_index);
		} else {
			m_reportManager.storeHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
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
	public ReportManager<HeartbeatReport> getReportManager() {
		return m_reportManager;
	}

	@Override
	protected void loadReports() {
		m_reportManager.loadHourlyReports(getStartTime(), StoragePolicy.FILE, m_index);
	}

	@Override
	protected void process(MessageTree tree) {
		String domain = tree.getDomain();

		if (m_serverFilterConfigManager.validateDomain(domain)) {
			Message message = tree.getMessage();
			HeartbeatReport report = findOrCreateReport(domain);
			report.addIp(tree.getIpAddress());

			if (message instanceof Transaction) {
				processTransaction(report, tree, (Transaction) message);
			}
		}
	}

	private void processHeartbeat(HeartbeatReport report, Heartbeat heartbeat, MessageTree tree) {
		String ip = tree.getIpAddress();
		Machine machine = report.findOrCreateMachine(ip);
		Period period = buildHeartBeatInfo(machine, heartbeat, heartbeat.getTimestamp());

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

	private void transalteHearbeat(StatusInfo info) {
		MessageInfo message = info.getMessage();

		if (message.getProduced() > 0 || message.getBytes() > 0) {
			Extension catExtension = info.findOrCreateExtension("CatUsage");

			catExtension.findOrCreateExtensionDetail("Produced").setValue(message.getProduced());
			catExtension.findOrCreateExtensionDetail("Overflowed").setValue(message.getOverflowed());
			catExtension.findOrCreateExtensionDetail("Bytes").setValue(message.getBytes());

			Extension system = info.findOrCreateExtension("System");
			OsInfo osInfo = info.getOs();

			system.findOrCreateExtensionDetail("LoadAverage").setValue(osInfo.getSystemLoadAverage());
			system.findOrCreateExtensionDetail("FreePhysicalMemory").setValue(osInfo.getFreePhysicalMemory());
			system.findOrCreateExtensionDetail("FreeSwapSpaceSize").setValue(osInfo.getFreeSwapSpace());

			Extension gc = info.findOrCreateExtension("GC");
			MemoryInfo memory = info.getMemory();
			List<GcInfo> gcs = memory.getGcs();

			if (gcs.size() >= 2) {
				GcInfo newGc = gcs.get(0);
				GcInfo oldGc = gcs.get(1);
				gc.findOrCreateExtensionDetail("ParNewCount").setValue(newGc.getCount());
				gc.findOrCreateExtensionDetail("ParNewTime").setValue(newGc.getTime());
				gc.findOrCreateExtensionDetail("ConcurrentMarkSweepCount").setValue(oldGc.getCount());
				gc.findOrCreateExtensionDetail("ConcurrentMarkSweepTime").setValue(oldGc.getTime());
			}

			Extension thread = info.findOrCreateExtension("FrameworkThread");
			ThreadsInfo threadInfo = info.getThread();

			thread.findOrCreateExtensionDetail("HttpThread").setValue(threadInfo.getHttpThreadCount());
			thread.findOrCreateExtensionDetail("CatThread").setValue(threadInfo.getCatThreadCount());
			thread.findOrCreateExtensionDetail("PigeonThread").setValue(threadInfo.getPigeonThreadCount());
			thread.findOrCreateExtensionDetail("ActiveThread").setValue(threadInfo.getCount());
			thread.findOrCreateExtensionDetail("StartedThread").setValue(threadInfo.getTotalStartedCount());

			Extension disk = info.findOrCreateExtension("Disk");
			List<DiskVolumeInfo> diskVolumes = info.getDisk().getDiskVolumes();

			for (DiskVolumeInfo vinfo : diskVolumes) {
				disk.findOrCreateExtensionDetail(vinfo.getId() + " Free").setValue(vinfo.getFree());
			}
		}

		for (Extension ex : info.getExtensions().values()) {
			Map<String, String> propertis = ex.getDynamicAttributes();

			for (Entry<String, String> entry : propertis.entrySet()) {
				try {
					double value = Double.parseDouble(entry.getValue());

					ex.findOrCreateExtensionDetail(entry.getKey()).setValue(value);
				} catch (Exception e) {
					Cat.logError("StatusExtension can only be double type", e);
				}
			}
		}
	}

}
