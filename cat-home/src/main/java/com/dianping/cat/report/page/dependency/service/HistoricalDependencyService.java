package com.dianping.cat.report.page.dependency.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalDependencyService extends BaseHistoricalModelService<DependencyReport> {

	@Inject
	private DependencyReportService m_reportService;

	public HistoricalDependencyService() {
		super(DependencyAnalyzer.ID);
	}

	@Override
	protected DependencyReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		DependencyReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private DependencyReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
