package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.sender.AlertConstants;

public class ExceptionContactor extends ProjectContactor {

	public static final String ID = AlertConstants.EXCEPTION;

	@Override
	public String getId() {
		return ID;
	}

}
