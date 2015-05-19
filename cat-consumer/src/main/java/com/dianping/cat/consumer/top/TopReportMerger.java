package com.dianping.cat.consumer.top;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Machine;
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
	protected void mergeError(Error old, Error error) {
		old.setCount(old.getCount() + error.getCount());
	}

	@Override
   protected void mergeMachine(Machine to, Machine from) {
		to.setCount(to.getCount() + from.getCount());
   }

	@Override
	protected void mergeSegment(Segment old, Segment segment) {
		old.setError(old.getError() + segment.getError());
	}

	@Override
	protected void mergeTopReport(TopReport old, TopReport topReport) {
		super.mergeTopReport(old, topReport);
	}

}
