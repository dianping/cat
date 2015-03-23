package com.dianping.cat.report.page.storage.task;

import com.dianping.cat.consumer.storage.StorageReportMerger;
import com.dianping.cat.consumer.storage.model.entity.Operation;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;

public class HistoryStorageReportMerger extends StorageReportMerger {

	public HistoryStorageReportMerger(StorageReport storageReport) {
		super(storageReport);
	}

	@Override
	public void visitOperation(Operation from) {
		from.getSegments().clear();
		super.visitOperation(from);
	}

}
