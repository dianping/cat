package com.dianping.cat.report.task.metric;

public class FetchMetricReportException extends RuntimeException {

   private static final long serialVersionUID = 6962363899869779560L;

	public FetchMetricReportException() {
		super("fetch metric report error from http api!");
	}

	public FetchMetricReportException(String msg) {
		super(msg);
	}

}
