package com.dianping.cat.report.task.heartbeat;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultMerger;

public class HeartbeatDailyMerger extends DefaultMerger {

	private int m_mergedReportIndex = -1;

	public HeartbeatDailyMerger(HeartbeatReport heartbeatReport) {
		super(heartbeatReport);
	}

	@Override
	public void visitHeartbeatReport(HeartbeatReport from) {
		m_mergedReportIndex++;
		super.visitHeartbeatReport(from);
	}

	@Override
	protected void visitMachineChildren(Machine to, Machine from) {
		for (Period source : from.getPeriods()) {
			int minute = source.getMinute();

			source.setMinute(60 * m_mergedReportIndex + minute);
		}
		super.visitMachineChildren(to, from);
	}
}
