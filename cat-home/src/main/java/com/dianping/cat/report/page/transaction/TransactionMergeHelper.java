package com.dianping.cat.report.page.transaction;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.site.lookup.util.StringUtils;

public class TransactionMergeHelper {

	public TransactionReport mergerAllIp(TransactionReport report, String ipAddress) {
		if (StringUtils.isEmpty(ipAddress) ||Constants.ALL.equalsIgnoreCase(ipAddress)) {
			AllMachineMerger all = new AllMachineMerger();

			all.visitTransactionReport(report);
			report = all.getReport();
		}
		return report;
	}

	public TransactionReport mergerAllName(TransactionReport report, String allName) {
		if (StringUtils.isEmpty(allName) || Constants.ALL.equalsIgnoreCase(allName)) {
			AllNameMerger all = new AllNameMerger();

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
