package com.dianping.cat.report.task.browser;

import com.dianping.cat.home.browser.entity.BrowserReport;
import com.dianping.cat.home.browser.entity.UserAgent;
import com.dianping.cat.home.browser.transform.DefaultMerger;

public class BrowserReportMerger extends DefaultMerger {

	public BrowserReportMerger(BrowserReport browserReport) {
		super(browserReport);
	}

	@Override
	protected void mergeUserAgent(UserAgent old, UserAgent other) {
		old.setCount(old.getCount()+other.getCount());
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
