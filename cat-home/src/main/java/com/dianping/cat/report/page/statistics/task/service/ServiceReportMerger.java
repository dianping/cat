package com.dianping.cat.report.page.statistics.task.service;

import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.service.transform.DefaultMerger;

public class ServiceReportMerger extends DefaultMerger {

	public ServiceReportMerger(ServiceReport serviceReport) {
		super(serviceReport);
	}

	@Override
	protected void mergeDomain(Domain to, Domain from) {
		to.setTotalCount(to.getTotalCount() + from.getTotalCount());
		to.setFailureCount(to.getFailureCount() + from.getFailureCount());
		to.setFailurePercent(to.getFailureCount() * 1.0 / to.getTotalCount());
		to.setSum(to.getSum() + from.getSum());
		to.setAvg(to.getSum() / to.getTotalCount());
	}

	@Override
	protected void mergeServiceReport(ServiceReport to, ServiceReport from) {
		to.setStartTime(from.getStartTime());
		to.setEndTime(from.getEndTime());
		to.setDomain(from.getDomain());
		super.mergeServiceReport(to, from);
	}

}
