package com.dianping.cat.report.task;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.report.page.model.event.EventReportMerger;

public class HistoryEventReportMerger extends EventReportMerger{

	public HistoryEventReportMerger(EventReport eventReport) {
	   super(eventReport);
   }

	@Override
   public void visitRange(Range range) {
	   //super.visitRange(range);
   }

	@Override
   protected void visitRangeChildren(Range old, Range range) {
	   //super.visitRangeChildren(old, range);
   }
}
