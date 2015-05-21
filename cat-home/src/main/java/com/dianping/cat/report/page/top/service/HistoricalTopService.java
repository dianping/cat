package com.dianping.cat.report.page.top.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalTopService extends BaseHistoricalModelService<TopReport> {

	@Inject
	private TopReportService m_reportService;

	public HistoricalTopService() {
		super(TopAnalyzer.ID);
	}

	@Override
	protected TopReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		TopReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private TopReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
