package com.dianping.cat.report.page.statistics.task.service;

import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.service.transform.DefaultMerger;

public class ServiceReportMerger extends DefaultMerger {

	public ServiceReportMerger(ServiceReport serviceReport) {
		super(serviceReport);
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		old.setTotalCount(old.getTotalCount() + domain.getTotalCount());
		old.setFailureCount(old.getFailureCount() + domain.getFailureCount());
		old.setFailurePercent(old.getFailureCount() * 1.0 / old.getTotalCount());
		old.setSum(old.getSum() + domain.getSum());
		old.setAvg(old.getSum() / old.getTotalCount());
	}

	@Override
	protected void mergeServiceReport(ServiceReport old, ServiceReport bugReport) {
		old.setStartTime(bugReport.getStartTime());
		old.setEndTime(bugReport.getEndTime());
		old.setDomain(bugReport.getDomain());
		super.mergeServiceReport(old, bugReport);
	}

}
