package com.dianping.cat.problem.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.problem.analyzer.ProblemAnalyzer;
import com.dianping.cat.problem.model.entity.ProblemReport;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalProblemService extends BaseHistoricalModelService<ProblemReport> {

	@Inject
	private ProblemReportService m_reportService;

	public HistoricalProblemService() {
		super(ProblemAnalyzer.ID);
	}

	@Override
	protected ProblemReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		ProblemReport report = getReportFromDatabase(date, domain);
		;

		return report;
	}

	private ProblemReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
