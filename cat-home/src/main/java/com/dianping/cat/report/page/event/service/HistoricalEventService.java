package com.dianping.cat.report.page.event.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalEventService extends BaseHistoricalModelService<EventReport> {

	@Inject
	private EventReportService m_reportService;

	public HistoricalEventService() {
		super(EventAnalyzer.ID);
	}

	@Override
	protected EventReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		EventReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private EventReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
