package com.dianping.cat.report.page.cross.service;

import java.util.List;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossReportMerger;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeCrossService extends BaseCompositeModelService<CrossReport> {
	public CompositeCrossService() {
		super(CrossAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<CrossReport> createRemoteService() {
		return new RemoteCrossService();
	}

	@Override
	protected CrossReport merge(ModelRequest request, List<ModelResponse<CrossReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(request.getDomain()));
		for (ModelResponse<CrossReport> response : responses) {
			CrossReport model = response.getModel();
			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getCrossReport();
	}
}
