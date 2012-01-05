package com.dianping.cat.message.consumer.failure;

import com.dianping.cat.consumer.model.failure.entity.FailureReport;
import com.dianping.cat.consumer.model.failure.transform.DefaultJsonBuilder;

public class FailJSONReportStore implements FailReportStore{

	@Override
	public void storeFailureReport(FailureReport report) {
		DefaultJsonBuilder jsonBuilder = new DefaultJsonBuilder();
		jsonBuilder.visitFailureReport(report);
		System.out.println(jsonBuilder.getString());		
	}
}
