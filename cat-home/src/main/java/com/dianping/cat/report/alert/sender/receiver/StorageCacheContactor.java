package com.dianping.cat.report.alert.sender.receiver;

import com.dianping.cat.report.alert.AlertType;

public class StorageCacheContactor extends AbstractStorageContactor {

	public static final String ID = AlertType.STORAGE_CACHE.getName();

	@Override
	public String getId() {
		return ID;
	}

}
