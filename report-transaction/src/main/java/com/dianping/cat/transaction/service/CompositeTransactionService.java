package com.dianping.cat.transaction.service;

import java.util.List;

import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.transaction.analyzer.TransactionAnalyzer;
import com.dianping.cat.transaction.analyzer.TransactionReportMerger;
import com.dianping.cat.transaction.model.entity.TransactionReport;

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
