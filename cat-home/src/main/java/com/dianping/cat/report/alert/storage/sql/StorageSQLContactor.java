package com.dianping.cat.report.alert.storage.sql;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.report.alert.storage.AbstractStorageContactor;

public class StorageSQLContactor extends AbstractStorageContactor {

	public static final String ID = AlertType.STORAGE_SQL.getName();

	@Override
	public String getId() {
		return ID;
	}
}
