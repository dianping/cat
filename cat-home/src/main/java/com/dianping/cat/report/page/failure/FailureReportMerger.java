package com.dianping.cat.report.page.failure;

import com.dianping.cat.consumer.failure.model.entity.Entry;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.entity.Threads;
import com.dianping.cat.consumer.failure.model.transform.DefaultMerger;

public class FailureReportMerger extends DefaultMerger {
	public FailureReportMerger(FailureReport failureReport) {
		super(failureReport);
	}

	@Override
	protected void mergeEntry(Entry old, Entry entry) {
		// TODO Auto-generated method stub
		super.mergeEntry(old, entry);
	}

	@Override
	protected void mergeThreads(Threads old, Threads threads) {
		old.getThreads().addAll(threads.getThreads());
	}
}
