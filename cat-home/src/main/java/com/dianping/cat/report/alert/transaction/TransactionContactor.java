package com.dianping.cat.report.alert.transaction;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;

public class TransactionContactor extends ProjectContactor {

	public static final String ID = AlertType.Transaction.getName();

	@Override
	public String getId() {
		return ID;
	}

}
