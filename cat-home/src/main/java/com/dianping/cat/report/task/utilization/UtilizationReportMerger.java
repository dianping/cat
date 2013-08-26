package com.dianping.cat.report.task.utilization;

import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.home.utilization.transform.DefaultMerger;

public class UtilizationReportMerger extends DefaultMerger {

	public UtilizationReportMerger(UtilizationReport utilizationReport) {
		super(utilizationReport);
	}

	@Override
	protected void mergeUtilizationReport(UtilizationReport old, UtilizationReport bugReport) {
		old.setStartTime(bugReport.getStartTime());
		old.setEndTime(bugReport.getEndTime());
		old.setDomain(bugReport.getDomain());
		super.mergeUtilizationReport(old, bugReport);
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		if (domain.getMachineNumber() > old.getMachineNumber()) {
			old.setMachineNumber(domain.getMachineNumber());
		}
		old.setUrlCount(old.getUrlCount() + domain.getUrlCount());
		old.setUrlResponseTime(old.getUrlResponseTime() + domain.getUrlResponseTime());
		old.setSqlCount(old.getSqlCount() + domain.getSqlCount());
		old.setPigeonCallCount(old.getPigeonCallCount() + domain.getPigeonCallCount());
		old.setSwallowCallCount(old.getSwallowCallCount() + domain.getSwallowCallCount());
		old.setMemcacheCount(old.getMemcacheCount() + domain.getMemcacheCount());
	}

}
