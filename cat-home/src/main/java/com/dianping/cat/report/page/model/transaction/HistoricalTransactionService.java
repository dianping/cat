package com.dianping.cat.report.page.model.transaction;

import java.util.Date;
import java.util.List;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.hadoop.dal.ReportDao;
import com.dianping.cat.hadoop.dal.ReportEntity;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.internal.BaseHistoricalModelService;
import com.site.lookup.annotation.Inject;

public class HistoricalTransactionService extends BaseHistoricalModelService<TransactionReport> {
	@Inject
	private ReportDao m_reportDao;

	public HistoricalTransactionService() {
		super("transaction");
	}

	@Override
	protected TransactionReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = Long.parseLong(request.getProperty("date"));
		List<Report> reports = m_reportDao.findAllByPeriodDomainTypeName(new Date(date), domain, 1, getName(),
		      ReportEntity.READSET_FULL);
		TransactionReportMerger merger = null;

		for (Report report : reports) {
			String xml = report.getContent();
			TransactionReport model = new DefaultXmlParser().parse(xml);

			if (merger == null) {
				merger = new TransactionReportMerger(model);
			} else {
				model.accept(merger);
			}
		}

		return merger.getTransactionReport();
	}
}
