package com.dianping.cat.report.page.model.top;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultMerger;

public class TopReportMerger extends DefaultMerger {

	public TopReportMerger(TopReport topReport) {
		super(topReport);
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		super.mergeDomain(old, domain);
	}

	@Override
	protected void mergeSegment(Segment old, Segment segment) {
		old.setError(old.getError() + segment.getError());
		old.setCallError(old.getCallError() + segment.getCallError());
		
		old.setCache(old.getCache() + segment.getCache());
		old.setCacheSum(old.getCacheSum() + segment.getCacheSum());
		if (old.getCache() > 0) {
			old.setCacheDuration(old.getCacheSum() / old.getCache());
		}
		old.setUrl(old.getUrl() + segment.getUrl());
		old.setUrlSum(old.getUrlSum() + segment.getUrlSum());
		if (old.getUrl() > 0) {
			old.setUrlDuration(old.getUrlSum() / old.getUrl());
		}
		old.setSql(old.getSql() + segment.getSql());
		old.setSqlSum(old.getSqlSum() + segment.getSqlSum());
		if (old.getSql() > 0) {
			old.setSqlDuration(old.getSqlSum() / old.getSql());
		}
		old.setCall(old.getCall() + segment.getCall());
		old.setCallSum(old.getCallSum() + segment.getCallSum());
		if (old.getCall() > 0) {
			old.setCallDuration(old.getCallSum() / old.getCall());
		}
		old.setService(old.getService() + segment.getService());
		old.setServiceSum(old.getServiceSum() + segment.getServiceSum());
		if (old.getService() > 0) {
			old.setServiceDuration(old.getServiceSum() / old.getService());
		}
	}

	@Override
	protected void mergeTopReport(TopReport old, TopReport topReport) {
		super.mergeTopReport(old, topReport);
	}

}
