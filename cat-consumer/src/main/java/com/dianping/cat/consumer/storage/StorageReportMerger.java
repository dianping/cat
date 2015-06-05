package com.dianping.cat.consumer.storage;

import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.Segment;
import com.dianping.cat.consumer.storage.model.entity.Sql;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.consumer.storage.model.transform.DefaultMerger;

public class StorageReportMerger extends DefaultMerger {

	public StorageReportMerger(StorageReport storageReport) {
		super(storageReport);
	}

	@Override
	protected void mergeOperation(Operation to, Operation from) {
		to.setCount(to.getCount() + from.getCount());
		to.setLongCount(to.getLongCount() + from.getLongCount());
		to.setError(to.getError() + from.getError());
		to.setSum(to.getSum() + from.getSum());
		to.setAvg(to.getSum() / to.getCount());
	}

	@Override
	protected void mergeSegment(Segment to, Segment from) {
		to.setCount(to.getCount() + from.getCount());
		to.setLongCount(to.getLongCount() + from.getLongCount());
		to.setError(to.getError() + from.getError());
		to.setSum(to.getSum() + from.getSum());
		to.setAvg(to.getSum() / to.getCount());
	}

	@Override
	protected void mergeSql(Sql to, Sql from) {
		to.setId(from.getId());
		to.setCount(to.getCount() + from.getCount());
		to.setStatement(from.getStatement());
	}
}
