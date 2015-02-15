package com.dianping.cat.report.alert.sender.receiver;

import com.dianping.cat.report.alert.AlertType;

public class ThirdpartyContactor extends ProjectContactor {

	public static final String ID = AlertType.ThirdParty.getName();

	@Override
	public String getId() {
		return ID;
	}

}
