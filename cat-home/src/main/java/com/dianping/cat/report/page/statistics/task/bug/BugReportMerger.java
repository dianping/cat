package com.dianping.cat.report.page.statistics.task.bug;

import java.util.List;

import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.entity.Domain;
import com.dianping.cat.home.bug.entity.ExceptionItem;
import com.dianping.cat.home.bug.transform.DefaultMerger;

public class BugReportMerger extends DefaultMerger {

	public BugReportMerger(BugReport bugReport) {
		super(bugReport);
	}

	@Override
	protected void mergeBugReport(BugReport old, BugReport bugReport) {
		old.setStartTime(bugReport.getStartTime());
		old.setEndTime(bugReport.getEndTime());
		old.setDomain(bugReport.getDomain());
		super.mergeBugReport(old, bugReport);
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		old.setProblemUrl(domain.getProblemUrl());
		super.mergeDomain(old, domain);
	}

	@Override
	protected void mergeExceptionItem(ExceptionItem old, ExceptionItem exceptionItem) {
		old.setCount(old.getCount() + exceptionItem.getCount());
//		old.getMessages().addAll(exceptionItem.getMessages());

		List<String> oldMessages = old.getMessages();
		List<String> newMessages = exceptionItem.getMessages();

		mergeList(oldMessages, newMessages, 10);
	}

	protected void mergeList(List<String> oldMessages, List<String> newMessages, int size) {
		int originalSize = oldMessages.size();

		if (originalSize < size) {
			int remainingSize = size - originalSize;

			if (remainingSize >= newMessages.size()) {
				oldMessages.addAll(newMessages);
			} else {
				oldMessages.addAll(newMessages.subList(0, remainingSize));
			}
		}
	}

}
