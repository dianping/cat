package com.dianping.cat.consumer.browser;

import com.dianping.cat.consumer.browser.model.entity.BrowserReport;
import com.dianping.cat.consumer.browser.model.entity.UserAgent;
import com.dianping.cat.consumer.browser.model.transform.DefaultMerger;

public class BrowserReportMerger extends DefaultMerger {

	public BrowserReportMerger(BrowserReport browserReport) {
		super(browserReport);
	}

	@Override
	protected void mergeUserAgent(UserAgent old, UserAgent other) {
		if (old.getCount() == null) {
			old.setCount(0);
		}
		if (other.getCount() != null) {
			old.setCount(old.getCount() + other.getCount());
		}
	}

	@Override
	public void visitBrowserReport(BrowserReport browserReport) {
		BrowserReport oldReport = getBrowserReport();

		oldReport.setDomain(browserReport.getDomain());
		oldReport.setStartTime(browserReport.getStartTime());
		oldReport.setEndTime(browserReport.getEndTime());
		oldReport.getDomainNames().addAll(browserReport.getDomainNames());
		super.visitBrowserReport(browserReport);
	}
}
