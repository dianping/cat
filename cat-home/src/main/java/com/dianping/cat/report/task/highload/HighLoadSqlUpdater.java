package com.dianping.cat.report.task.highload;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;

public class HighLoadSqlUpdater extends TransactionHighLoadUpdater {

	public static final String ID = Constants.HIGH_LOAD_SQL;

	@Override
	public double calWeight(TransactionName name) {
		return name.getTotalCount() * name.getAvg();
	}

	@Override
	public String getID() {
		return ID;
	}

	@Override
	public String getType() {
		return "SQL";
	}
}