package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertConstants;

public class ThirdpartyContactor extends ProjectContactor {

	public static final String ID = AlertConstants.THIRDPARTY;

	@Override
	public String getId() {
		return ID;
	}

}
