package com.dianping.cat.report.page.top.service;

import java.util.List;

import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopReportMerger;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeTopService extends BaseCompositeModelService<TopReport> {
	public CompositeTopService() {
		super(TopAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<TopReport> createRemoteService() {
		return new RemoteTopService();
	}

	@Override
	protected TopReport merge(ModelRequest request, List<ModelResponse<TopReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		TopReportMerger merger = new TopReportMerger(new TopReport(request.getDomain()));
		for (ModelResponse<TopReport> response : responses) {
			TopReport model = response.getModel();

			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getTopReport();
	}
}
