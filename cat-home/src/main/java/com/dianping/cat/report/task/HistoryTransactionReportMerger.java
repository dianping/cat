package com.dianping.cat.report.task;

import com.dianping.cat.consumer.transaction.model.entity.AllDuration;
import com.dianping.cat.consumer.transaction.model.entity.Duration;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.page.model.transaction.TransactionReportMerger;

public class HistoryTransactionReportMerger extends TransactionReportMerger {

	public HistoryTransactionReportMerger(TransactionReport transactionReport) {
	   super(transactionReport);
   }

	@Override
   public void visitAllDuration(AllDuration allDuration) {
		//super.visitAllDuration(allDuration);
   }

	@Override
   public void visitDuration(Duration duration) {
		//super.visitDuration(duration);
   }

	@Override
   public void visitRange(Range range) {
	  // super.visitRange(range);
   }
	
}
