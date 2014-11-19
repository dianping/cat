package com.dianping.cat.consumer.heartbeat;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultMerger;

public class HeartbeatReportMerger extends DefaultMerger {
	public HeartbeatReportMerger(HeartbeatReport heartbeatReport) {
		super(heartbeatReport);
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
		old.addProperty(period.findProperty("CatMessageOverflow"));
		old.addProperty(period.findProperty("CatMessageProduced"));
		old.addProperty(period.findProperty("CatMessageSize"));
		old.addProperty(period.findProperty("CatThreadCount"));
		old.addProperty(period.findProperty("DaemonCount"));
		old.addProperty(period.findProperty("HeapUsage"));
		old.addProperty(period.findProperty("HttpThreadCount"));
		old.addProperty(period.findProperty("MemoryFree"));
		old.addProperty(period.findProperty("Minute"));
		old.addProperty(period.findProperty("NewGcCount"));
		old.addProperty(period.findProperty("NoneHeapUsage"));
		old.addProperty(period.findProperty("OldGcCount"));
		old.addProperty(period.findProperty("PigeonThreadCount"));
		old.addProperty(period.findProperty("SystemLoadAverage"));
		old.addProperty(period.findProperty("ThreadCount"));
		old.addProperty(period.findProperty("TotalStartedCount"));
	}

	@Override
	public void visitHeartbeatReport(HeartbeatReport heartbeatReport) {
		super.visitHeartbeatReport(heartbeatReport);

		getHeartbeatReport().getDomainNames().addAll(heartbeatReport.getDomainNames());
		getHeartbeatReport().getIps().addAll(heartbeatReport.getIps());
	}

}
