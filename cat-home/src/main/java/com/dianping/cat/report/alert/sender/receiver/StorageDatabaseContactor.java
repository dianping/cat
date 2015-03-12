package com.dianping.cat.report.alert.sender.receiver;

import com.dianping.cat.report.alert.AlertType;

public class StorageDatabaseContactor extends ProjectContactor {

	public static final String ID = AlertType.STORAGE_SQL.getName();

	@Override
	public String getId() {
		return ID;
	}

}
