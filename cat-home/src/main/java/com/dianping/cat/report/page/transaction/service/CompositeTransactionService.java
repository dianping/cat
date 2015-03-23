package com.dianping.cat.report.page.transaction.service;

import java.util.List;

import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeTransactionService extends BaseCompositeModelService<TransactionReport> {
	public CompositeTransactionService() {
		super(TransactionAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<TransactionReport> createRemoteService() {
		return new RemoteTransactionService();
	}

	@Override
	protected TransactionReport merge(ModelRequest request, List<ModelResponse<TransactionReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(request.getDomain()));

		for (ModelResponse<TransactionReport> response : responses) {
			if (response != null) {
				TransactionReport model = response.getModel();
				if (model != null) {
					model.accept(merger);
				}
			}
		}
		return merger.getTransactionReport();
	}
}
