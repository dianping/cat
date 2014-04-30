package com.dianping.cat.report.task.exceptionAlert;

import com.dianping.cat.home.alertReport.entity.AlertReport;
import com.dianping.cat.home.alertReport.entity.Domain;
import com.dianping.cat.home.alertReport.entity.Exception;
import com.dianping.cat.home.alertReport.transform.DefaultMerger;

public class AlertReportMerger extends DefaultMerger {
	
	public AlertReportMerger(AlertReport alertReport){
		super(alertReport);
	}
	
	@Override
   public void mergeException(Exception old, Exception from) {
		old.setWarnNumber(old.getWarnNumber() + from.getWarnNumber());
		old.setErrorNumber(old.getErrorNumber() + from.getErrorNumber());
	}
	
	@Override
   public void mergeDomain(Domain old, Domain from) {
		old.setWarnNumber(old.getWarnNumber() + from.getWarnNumber());
		old.setErrorNumber(old.getErrorNumber() + from.getErrorNumber());
	}
	
	@Override
   public void mergeAlertReport(AlertReport old, AlertReport from) {
		old.setDomain(from.getDomain());
		super.mergeAlertReport(old, from);
	}
}
