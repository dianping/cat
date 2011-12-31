package com.dianping.cat.message.consumer.failure;

import com.dianping.cat.consumer.failurereport.entity.FailureReport;

public interface FailReportStore {
	/**
	 * Store the total report
	 * @param report
	 */
	public void storeFailureReport(FailureReport report);
	
}
