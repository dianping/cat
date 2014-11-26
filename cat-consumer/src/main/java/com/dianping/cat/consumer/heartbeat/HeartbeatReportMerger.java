package com.dianping.cat.consumer.heartbeat;

import com.dianping.cat.consumer.heartbeat.model.entity.Disk;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultMerger;

public class HeartbeatReportMerger extends DefaultMerger {
	public HeartbeatReportMerger(HeartbeatReport heartbeatReport) {
		super(heartbeatReport);
	}

	private boolean isOldVersion(Period period) {
		if (period.getSystemLoadAverage() == 0 && period.getMemoryFree() == 0) {
			return false;
		} else {
			return true;
		}
	}

	@Override
	protected void mergeDisk(Disk old, Disk disk) {
		super.mergeDisk(old, disk);
	}

	@Override
	protected void mergeHeartbeatReport(HeartbeatReport old, HeartbeatReport heartbeatReport) {
		super.mergeHeartbeatReport(old, heartbeatReport);
	}

	@Override
	protected void mergeMachine(Machine old, Machine machine) {
		super.mergeMachine(old, machine);
	}

	@Override
	protected void mergePeriod(Period old, Period period) {
		if (isOldVersion(period)) {
			old.setCatMessageOverflow(period.getCatMessageOverflow());
			old.setCatMessageProduced(period.getCatMessageProduced());
			old.setCatMessageSize(period.getCatMessageSize());
			old.setCatThreadCount(period.getCatThreadCount());
			old.setDaemonCount(period.getDaemonCount());
			old.setHeapUsage(period.getHeapUsage());
			old.setHttpThreadCount(period.getHttpThreadCount());
			old.setMemoryFree(period.getMemoryFree());
			old.setMinute(period.getMinute());
			old.setNewGcCount(period.getNewGcCount());
			old.setNoneHeapUsage(period.getNoneHeapUsage());
			old.setOldGcCount(period.getOldGcCount());
			old.setPigeonThreadCount(period.getPigeonThreadCount());
			old.setSystemLoadAverage(period.getSystemLoadAverage());
			old.setThreadCount(period.getThreadCount());
			old.setTotalStartedCount(period.getTotalStartedCount());
		} else {
			old.setMinute(period.getMinute());
			old.addExtension(period.getExtensions().get("OS"));
			old.addExtension(period.getExtensions().get("DISK FREE"));
			old.addExtension(period.getExtensions().get("MESSAGE"));
			old.addExtension(period.getExtensions().get("RUNTIME"));
			old.addExtension(period.getExtensions().get("THREAD"));
			old.addExtension(period.getExtensions().get("MEMORY"));
		}
	}

	@Override
	public void visitHeartbeatReport(HeartbeatReport heartbeatReport) {
		super.visitHeartbeatReport(heartbeatReport);

		getHeartbeatReport().getDomainNames().addAll(heartbeatReport.getDomainNames());
		getHeartbeatReport().getIps().addAll(heartbeatReport.getIps());
	}

}
