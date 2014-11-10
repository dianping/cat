package com.dianping.cat.report.task.alert.sender.receiver;

import com.dianping.cat.report.task.alert.AlertType;

public class TransactionContactor extends ProjectContactor {

	public static final String ID = AlertType.Transaction.getName();

	@Override
	public String getId() {
		return ID;
	}

}
