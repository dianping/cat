package com.dianping.cat.report.page.statistics.task.bug;

import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.entity.ExceptionItem;

public class HistoryBugReportMerger extends BugReportMerger {

	public HistoryBugReportMerger(BugReport bugReport) {
		super(bugReport);
	}

	@Override
	protected void mergeExceptionItem(ExceptionItem old, ExceptionItem exceptionItem) {
		old.setCount(old.getCount() + exceptionItem.getCount());
		old.getMessages().addAll(exceptionItem.getMessages());
	}
}
