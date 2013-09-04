package com.dianping.cat.consumer.heartbeat;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultMerger;

public class HeartbeatReportMerger extends DefaultMerger {
	public HeartbeatReportMerger(HeartbeatReport heartbeatReport) {
		super(heartbeatReport);
	}
	
	@Override
	public void visitHeartbeatReport(HeartbeatReport heartbeatReport) {
		super.visitHeartbeatReport(heartbeatReport);

		getHeartbeatReport().getDomainNames().addAll(heartbeatReport.getDomainNames());
		getHeartbeatReport().getIps().addAll(heartbeatReport.getIps());
	}
}
