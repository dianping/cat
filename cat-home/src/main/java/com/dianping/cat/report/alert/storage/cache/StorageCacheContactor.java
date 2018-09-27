package com.dianping.cat.report.alert.storage.cache;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.report.alert.storage.AbstractStorageContactor;

public class StorageCacheContactor extends AbstractStorageContactor {

	public static final String ID = AlertType.STORAGE_CACHE.getName();

	@Override
	public String getId() {
		return ID;
	}

}
