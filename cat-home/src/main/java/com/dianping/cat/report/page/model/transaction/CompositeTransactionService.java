package com.dianping.cat.report.page.model.transaction;

import java.util.List;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeTransactionService extends BaseCompositeModelService<TransactionReport> {
	public CompositeTransactionService() {
		super("transaction");
	}

	@Override
	protected BaseRemoteModelService<TransactionReport> createRemoteService() {
		return new RemoteTransactionService();
	}

	@Override
	protected TransactionReport merge(List<ModelResponse<TransactionReport>> responses) {
		TransactionReportMerger merger = null;

		for (ModelResponse<TransactionReport> response : responses) {
			if (response != null) {
				TransactionReport model = response.getModel();

				if (model != null) {
					if (merger == null) {
						merger = new TransactionReportMerger(model);
					} else {
						model.accept(merger);
					}
				}
			}
		}

		return merger == null ? null : merger.getTransactionReport();
	}
}
