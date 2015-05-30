package com.dianping.cat.transaction.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.transaction.analyzer.TransactionAnalyzer;
import com.dianping.cat.transaction.model.entity.TransactionReport;

public class HistoricalTransactionService extends BaseHistoricalModelService<TransactionReport> {

	@Inject
	private TransactionReportService m_reportService;

	public HistoricalTransactionService() {
		super(TransactionAnalyzer.ID);
	}

	@Override
	protected TransactionReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		TransactionReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private TransactionReport getReportFromDatabase(long timestamp, String domain) throws Exception {
		return m_reportService.queryReport(domain, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
