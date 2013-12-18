package com.dianping.cat.consumer.browser;

import com.dianping.cat.consumer.browsermeta.model.entity.BrowserMetaReport;
import com.dianping.cat.consumer.browsermeta.model.entity.UserAgent;
import com.dianping.cat.consumer.browsermeta.model.transform.DefaultMerger;

public class BrowserMetaReportMerger extends DefaultMerger {

	public BrowserMetaReportMerger(BrowserMetaReport metaReport) {
		super(metaReport);
	}

	@Override
   protected void mergeBrowserMetaReport(BrowserMetaReport to, BrowserMetaReport from) {
	   super.mergeBrowserMetaReport(to, from);
   }

	@Override
   protected void mergeUserAgent(UserAgent to, UserAgent from) {
		to.setCount(to.getCount()+from.getCount());
   }
	
}
