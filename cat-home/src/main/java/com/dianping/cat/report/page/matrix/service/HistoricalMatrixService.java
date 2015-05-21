package com.dianping.cat.report.page.matrix.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalMatrixService extends BaseHistoricalModelService<MatrixReport> {

	@Inject
	private MatrixReportService m_reportService;

	public HistoricalMatrixService() {
		super(MatrixAnalyzer.ID);
	}

	@Override
	protected MatrixReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		MatrixReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private MatrixReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
