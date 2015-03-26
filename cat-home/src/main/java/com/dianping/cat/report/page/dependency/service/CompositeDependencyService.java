package com.dianping.cat.report.page.dependency.service;

import java.util.List;

import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyReportMerger;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeDependencyService extends BaseCompositeModelService<DependencyReport> {
	public CompositeDependencyService() {
		super(DependencyAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<DependencyReport> createRemoteService() {
		return new RemoteDependencyService();
	}

	@Override
	protected DependencyReport merge(ModelRequest request, List<ModelResponse<DependencyReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		DependencyReportMerger merger = new DependencyReportMerger(new DependencyReport(request.getDomain()));
		for (ModelResponse<DependencyReport> response : responses) {
			DependencyReport model = response.getModel();

			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getDependencyReport();
	}
}
