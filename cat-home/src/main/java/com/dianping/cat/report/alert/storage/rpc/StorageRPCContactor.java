package com.dianping.cat.report.alert.storage.rpc;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;

public class StorageRPCContactor extends ProjectContactor {

	public static final String ID = AlertType.STORAGE_RPC.getName();

	@Override
	public String getId() {
		return ID;
	}

}
