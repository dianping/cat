package com.dianping.cat.report.page.cross.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalCrossService extends BaseHistoricalModelService<CrossReport> {

	@Inject
	private CrossReportService m_reportService;

	public HistoricalCrossService() {
		super(CrossAnalyzer.ID);
	}

	@Override
	protected CrossReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		CrossReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private CrossReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
