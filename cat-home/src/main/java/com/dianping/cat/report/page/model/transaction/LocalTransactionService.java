package com.dianping.cat.report.page.model.transaction;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.page.model.spi.internal.BaseLocalModelService;

public class LocalTransactionService extends BaseLocalModelService<TransactionReport> {
	public LocalTransactionService() {
		super("transaction");
	}
}
