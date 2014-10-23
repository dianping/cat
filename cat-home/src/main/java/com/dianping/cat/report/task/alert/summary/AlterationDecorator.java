package com.dianping.cat.report.task.alert.summary;

public class AlterationDecorator extends SummaryDecorator {

	public static final String ID = "AlterationDecorator";

	@Override
	protected String getID() {
		return ID;
	}

	@Override
	protected String getTemplateAddress() {
		return "alterationInfo.ftl";
	}

}
