package com.dianping.cat.report.page.business.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalBusinessService extends BaseHistoricalModelService<BusinessReport> {

	@Inject
	private BusinessReportService m_reportService;

	public HistoricalBusinessService() {
		super(BusinessAnalyzer.ID);
	}

	@Override
	protected BusinessReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		BusinessReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private BusinessReport getReportFromDatabase(long date, String domain) {
		return m_reportService.queryReport(domain, new Date(date), new Date(date + TimeHelper.ONE_HOUR));
	}

}
