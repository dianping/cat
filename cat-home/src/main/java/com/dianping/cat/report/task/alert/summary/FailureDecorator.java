package com.dianping.cat.report.task.alert.summary;

public class FailureDecorator extends SummaryDecorator {

	public static final String ID = "FailureDecorator";

	@Override
	protected String getID() {
		return ID;
	}

	@Override
	protected String getTemplateAddress() {
		return "errorInfo.ftl";
	}

}
