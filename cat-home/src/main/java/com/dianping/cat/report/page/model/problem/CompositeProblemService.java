package com.dianping.cat.report.page.model.problem;

import java.util.List;

import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

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
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(request.getDomain()));

		for (ModelResponse<ProblemReport> response : responses) {
			if (response != null) {
				ProblemReport model = response.getModel();

				if (model != null) {
					if (merger == null) {
						merger = new ProblemReportMerger(model);
					} else {
						model.accept(merger);
					}
				}
			}
		}

		return merger == null ? null : merger.getProblemReport();
	}
}
