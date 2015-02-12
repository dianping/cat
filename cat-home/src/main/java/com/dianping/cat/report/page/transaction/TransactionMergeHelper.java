package com.dianping.cat.report.page.transaction;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;

public class TransactionMergeHelper {

	public TransactionReport mergeAllMachines(TransactionReport report, String ipAddress) {
		if (Constants.ALL.equalsIgnoreCase(ipAddress)) {
			AllMachineMerger all = new AllMachineMerger();

			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
	}

	private TransactionReport mergeAllNames(TransactionReport report, String allName) {
		if (Constants.ALL.equalsIgnoreCase(allName)) {
			AllNameMerger all = new AllNameMerger();

			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
	}

	public TransactionReport mergeAllNames(TransactionReport report, String ipAddress, String allName) {
		TransactionReport temp = mergeAllMachines(report, ipAddress);

		return mergeAllNames(temp, allName);
	}

}
