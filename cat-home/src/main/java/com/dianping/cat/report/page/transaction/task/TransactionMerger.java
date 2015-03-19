/**
 * 
 */
package com.dianping.cat.report.page.transaction.task;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.transaction.TransactionReportCountFilter;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.task.TaskHelper;

public class TransactionMerger {

	private TransactionReport merge(String reportDomain, List<TransactionReport> reports, double duration) {
		HistoryTransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(reportDomain))
		      .setDuration(duration);

		for (TransactionReport report : reports) {
			report.accept(merger);
		}

		return merger.getTransactionReport();
	}

	public TransactionReport mergeForDaily(String reportDomain, List<TransactionReport> reports, Set<String> domainSet,
	      double duration) {
		TransactionReport transactionReport = merge(reportDomain, reports, duration);
		TransactionReport transactionReport2 = merge(reportDomain, reports, duration);
		HistoryTransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(reportDomain))
		      .setDuration(duration);
		com.dianping.cat.consumer.transaction.model.entity.Machine allMachines = merger
		      .mergesForAllMachine(transactionReport2);

		transactionReport.addMachine(allMachines);
		transactionReport.getIps().add("All");
		transactionReport.getDomainNames().addAll(domainSet);

		Date date = transactionReport.getStartTime();
		transactionReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		transactionReport.setEndTime(end);

		new TransactionReportCountFilter().visitTransactionReport(transactionReport);
		return transactionReport;
	}

}
