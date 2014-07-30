package com.dianping.cat.report.task.alert.sender.decorator;

import com.dianping.cat.report.task.alert.AlertConstants;

public class FrontEndExceptionDecorator extends ExceptionDecorator {

	public static final String ID = AlertConstants.FRONT_END_EXCEPTION;

	@Override
	public String getId() {
		return ID;
	}

	@Override
	protected String buildContactInfo(String domainName) {
		return "";
	}

}
