/**
 * 
 */
package com.dianping.cat.report.task;

import java.io.IOException;
import java.util.List;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public class TransactionMerger implements ReportMerger<TransactionReport> {

	public String mergeAll(String reportDomain, List<Report> reports) {
		TransactionReport transactionReport = merge(reportDomain, reports);
		TransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(reportDomain));
		TransactionReport transactionReport2 = merge(reportDomain, reports);
		com.dianping.cat.consumer.transaction.model.entity.Machine allMachines = merger.mergesForAllMachine(transactionReport2);
		transactionReport.addMachine(allMachines);
		transactionReport.getIps().add("All");
		String content = transactionReport.toString();
		return content;
	}

	public TransactionReport merge(String reportDomain, List<Report> reports) {
		TransactionReport transactionReport;
		TransactionReportMerger merger = new HistoryTransactionReportMerger(new TransactionReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			TransactionReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		transactionReport = merger == null ? null : merger.getTransactionReport();
		return transactionReport;
	}
}
