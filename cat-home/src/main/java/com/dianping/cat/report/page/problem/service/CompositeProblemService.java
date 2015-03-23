package com.dianping.cat.report.page.problem.service;

import java.util.List;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeProblemService extends BaseCompositeModelService<ProblemReport> {
	public CompositeProblemService() {
		super(ProblemAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<ProblemReport> createRemoteService() {
		return new RemoteProblemService();
	}

	@Override
	protected ProblemReport merge(ModelRequest request, List<ModelResponse<ProblemReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(request.getDomain()));

		for (ModelResponse<ProblemReport> response : responses) {
			if (response != null) {
				ProblemReport model = response.getModel();

				if (model != null) {
					model.accept(merger);
				}
			}
		}

		return merger.getProblemReport();
	}
}
