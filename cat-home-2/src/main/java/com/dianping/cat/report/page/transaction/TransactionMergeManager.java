package com.dianping.cat.report.page.transaction;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public class TransactionMergeManager {

	public TransactionReport mergerAllIp(TransactionReport report, String ipAddress) {
		if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
			MergeAllMachine all = new MergeAllMachine();

			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
	}

	private TransactionReport mergerAllName(TransactionReport report, String allName) {
		if (Constants.ALL.equalsIgnoreCase(allName)) {
			MergeAllName all = new MergeAllName();

			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
	}

	public TransactionReport mergerAllName(TransactionReport report, String ipAddress, String allName) {
		TransactionReport temp = mergerAllIp(report, ipAddress);

		return mergerAllName(temp, allName);
	}

}
