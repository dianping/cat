package com.dianping.cat.consumer.browser;

import com.dianping.cat.consumer.browser.model.entity.Browser;
import com.dianping.cat.consumer.browser.model.entity.BrowserReport;
import com.dianping.cat.consumer.browser.model.entity.BrowserVersion;
import com.dianping.cat.consumer.browser.model.entity.DomainDetail;
import com.dianping.cat.consumer.browser.model.entity.Os;
import com.dianping.cat.consumer.browser.model.transform.DefaultMerger;

public class BrowserReportMerger extends DefaultMerger {

	public BrowserReportMerger(BrowserReport browserReport) {
		super(browserReport);
	}

	@Override
	protected void mergeDomainDetail(DomainDetail old, DomainDetail other) {
	}
	
	@Override
	protected void mergeBrowser(Browser old, Browser browser) {
		old.setCount(old.getCount() + browser.getCount());
	}
	
	@Override
	protected void mergeOs(Os old, Os os) {
		old.setCount(old.getCount() + os.getCount());
	}

	@Override
    protected void mergeBrowserVersion(BrowserVersion old, BrowserVersion browserVersion) {
		old.setCount(old.getCount() + browserVersion.getCount());
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
