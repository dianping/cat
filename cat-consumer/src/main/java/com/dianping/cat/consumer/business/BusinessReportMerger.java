package com.dianping.cat.consumer.business;

import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.entity.Segment;
import com.dianping.cat.consumer.business.model.transform.DefaultMerger;

public class BusinessReportMerger extends DefaultMerger {

	public BusinessReportMerger(BusinessReport businessReport) {
		super(businessReport);
	}

	@Override
	protected void mergeSegment(Segment old, Segment segment) {
		old.incCount(segment.getCount());
		old.incSum(segment.getSum());

		if (old.getCount() > 0) {
			old.setAvg(old.getSum() / old.getCount());
		}
	}
}
