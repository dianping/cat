/**
 * 
 */
package com.dianping.cat.report.task.transaction;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.transaction.TransactionReportMerger;
import com.dianping.cat.consumer.transaction.TransactionReportUrlFilter;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.task.TaskHelper;

public class TransactionMerger {

	private TransactionReport merge(String reportDomain, List<TransactionReport> reports, boolean isDaily) {
		TransactionReportMerger merger = null;
		if (isDaily) {
			merger = new HistoryTransactionReportMerger(new TransactionReport(reportDomain));
		} else {
			merger = new TransactionReportMerger(new TransactionReport(reportDomain));
		}
		for (TransactionReport report : reports) {
			report.accept(merger);
		}

		TransactionReport transactionReport = merger.getTransactionReport();
		return transactionReport;
	}

	public TransactionReport mergeForDaily(String reportDomain, List<TransactionReport> reports, Set<String> domainSet) {
		TransactionReport transactionReport = merge(reportDomain, reports, true);
		HistoryTransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(reportDomain));
		TransactionReport transactionReport2 = merge(reportDomain, reports, true);
		com.dianping.cat.consumer.transaction.model.entity.Machine allMachines = merger
		      .mergesForAllMachine(transactionReport2);
		transactionReport.addMachine(allMachines);
		transactionReport.getIps().add("All");
		transactionReport.getDomainNames().addAll(domainSet);

		Date date = transactionReport.getStartTime();
		transactionReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		transactionReport.setEndTime(end);

		new TransactionReportUrlFilter().visitTransactionReport(transactionReport);
		return transactionReport;
	}

	public TransactionReport mergeForGraph(String reportDomain, List<TransactionReport> reports) {
		TransactionReport transactionReport = merge(reportDomain, reports, false);
		TransactionReportMerger merger = new TransactionReportMerger(new TransactionReport(reportDomain));
		TransactionReport transactionReport2 = merge(reportDomain, reports, false);
		com.dianping.cat.consumer.transaction.model.entity.Machine allMachines = merger
		      .mergesForAllMachine(transactionReport2);
		transactionReport.addMachine(allMachines);
		transactionReport.getIps().add("All");

		new TransactionReportUrlFilter().visitTransactionReport(transactionReport);
		return transactionReport;
	}
}
