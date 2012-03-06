package com.dianping.cat.report.page.model.transaction;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;

public class HdfsTransactionModelService implements ModelService<TransactionReport> {
	@Override
	public boolean isEligable(ModelRequest request) {
		return request.getPeriod().isHistorical();
	}

	@Override
	public ModelResponse<TransactionReport> invoke(ModelRequest request) {
		// TODO
		return null;
	}
}
