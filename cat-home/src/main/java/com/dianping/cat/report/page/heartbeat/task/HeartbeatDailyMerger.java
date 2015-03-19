package com.dianping.cat.report.page.heartbeat.task;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultMerger;
import com.dianping.cat.helper.TimeHelper;

public class HeartbeatDailyMerger extends DefaultMerger {

	private long m_currentDay;

	private int m_hourIndex;

	public HeartbeatDailyMerger(HeartbeatReport heartbeatReport, long currentDay) {
		super(heartbeatReport);

		m_currentDay = currentDay;
	}

	@Override
	public void visitHeartbeatReport(HeartbeatReport from) {
		long start = from.getStartTime().getTime();
		m_hourIndex = (int) ((start - m_currentDay) / TimeHelper.ONE_HOUR);

		super.visitHeartbeatReport(from);
	}

	@Override
	protected void visitMachineChildren(Machine to, Machine from) {
		for (Period source : from.getPeriods()) {
			int minute = source.getMinute();

			source.setMinute(60 * m_hourIndex + minute);
		}
		super.visitMachineChildren(to, from);
	}
}
