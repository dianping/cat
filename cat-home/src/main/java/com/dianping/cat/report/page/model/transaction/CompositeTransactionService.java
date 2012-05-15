package com.dianping.cat.report.page.model.transaction;

import java.util.List;

import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.page.model.spi.ModelRequest;
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
	protected TransactionReport merge(ModelRequest request, List<ModelResponse<TransactionReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(request.getDomain()));
		String ip = request.getProperty("ip");
		if (ip.equals(CatString.ALL_IP)) {
			merger.setAllIp(true);
		}

		for (ModelResponse<TransactionReport> response : responses) {
			if (response != null) {
				TransactionReport model = response.getModel();
				model.accept(merger);
			}
		}

		TransactionReport report = merger.getTransactionReport();
		String all = request.getProperty("all");

		if ("true".equals(all)) {
			String type = request.getProperty("type");
			TransactionNameAggregator aggregator = new TransactionNameAggregator(report);
			TransactionName n = aggregator.mergesFor(type, ip);
			TransactionType t = new TransactionType(type).addName(n);
			TransactionReport result = new TransactionReport(request.getDomain());
			result.findOrCreateMachine(ip).addType(t);

			return result;
		} else {
			return report;
		}
	}
}
