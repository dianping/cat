package com.dianping.cat.report.page.heartbeat.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalHeartbeatService extends BaseHistoricalModelService<HeartbeatReport> {

	@Inject
	private HeartbeatReportService m_reportService;

	public HistoricalHeartbeatService() {
		super(HeartbeatAnalyzer.ID);
	}

	@Override
	protected HeartbeatReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		HeartbeatReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private HeartbeatReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
