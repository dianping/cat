package com.dianping.cat.report.task.utilization;

import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.home.utilization.transform.DefaultMerger;

public class UtilizationReportMerger extends DefaultMerger {

	public UtilizationReportMerger(UtilizationReport utilizationReport) {
		super(utilizationReport);
	}
	
	
	@Override
	public void visitUtilizationReport(UtilizationReport utilizationReport) {
		UtilizationReport oldReport = getUtilizationReport();
		
		oldReport.setDomain(utilizationReport.getDomain());
		oldReport.setStartTime(utilizationReport.getStartTime());
		oldReport.setEndTime(utilizationReport.getEndTime());
		super.visitUtilizationReport(utilizationReport);
	}

	@Override
	protected void mergeUtilizationReport(UtilizationReport old, UtilizationReport bugReport) {
		super.mergeUtilizationReport(old, bugReport);
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		if (domain.getMachineNumber() > old.getMachineNumber()) {
			old.setMachineNumber(domain.getMachineNumber());
		}
		old.setUrlCount(old.getUrlCount() + domain.getUrlCount());
		old.setUrlResponseTime(old.getUrlResponseTime() + domain.getUrlResponseTime());
		old.setServiceCount(old.getServiceCount() + domain.getServiceCount());
		old.setServiceResponseTime(old.getServiceResponseTime() + domain.getServiceResponseTime());
		old.setSqlCount(old.getSqlCount() + domain.getSqlCount());
		old.setPigeonCallCount(old.getPigeonCallCount() + domain.getPigeonCallCount());
		old.setSwallowCallCount(old.getSwallowCallCount() + domain.getSwallowCallCount());
		old.setMemcacheCount(old.getMemcacheCount() + domain.getMemcacheCount());
	}

}
