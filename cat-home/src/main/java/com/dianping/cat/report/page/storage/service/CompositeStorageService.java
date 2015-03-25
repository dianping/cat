package com.dianping.cat.report.page.storage.service;

import java.util.List;

import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageReportMerger;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeStorageService extends BaseCompositeModelService<StorageReport> {
	public CompositeStorageService() {
		super(StorageAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<StorageReport> createRemoteService() {
		return new RemoteStorageService();
	}

	@Override
	protected StorageReport merge(ModelRequest request, List<ModelResponse<StorageReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		StorageReportMerger merger = new StorageReportMerger(new StorageReport(request.getDomain()));

		for (ModelResponse<StorageReport> response : responses) {
			if (response != null) {
				StorageReport model = response.getModel();
				if (model != null) {
					model.accept(merger);
				}
			}
		}
		return merger.getStorageReport();
	}
}
