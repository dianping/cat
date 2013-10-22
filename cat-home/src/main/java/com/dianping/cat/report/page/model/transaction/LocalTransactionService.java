package com.dianping.cat.report.page.model.transaction;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.ModelPeriod;
import com.dianping.cat.service.ModelRequest;

public class LocalTransactionService extends BaseLocalModelService<TransactionReport> {
	@Inject
	private ReportService m_reportService;

	public LocalTransactionService() {
		super(TransactionAnalyzer.ID);
	}

	@Override
	protected TransactionReport getReport(ModelRequest request, ModelPeriod period, String domain) throws Exception {
		TransactionReport report = super.getReport(request, period, domain);

		if (report == null && period.isLast()) {
			long startTime = request.getStartTime();
			Date start = new Date(startTime);
			Date end = new Date(startTime + TimeUtil.ONE_HOUR);

			report = m_reportService.queryTransactionReport(domain, start, end);
		
		}
		return report;
	}
}
