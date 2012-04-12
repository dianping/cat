package com.dianping.cat.report.page.model.problem;

import java.util.List;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeProblemService extends BaseCompositeModelService<ProblemReport> {
	public CompositeProblemService() {
		super("problem");
	}

	@Override
	protected BaseRemoteModelService<ProblemReport> createRemoteService() {
		return new RemoteProblemService();
	}

	@Override
	protected ProblemReport merge(ModelRequest request, List<ModelResponse<ProblemReport>> responses) {
		ProblemReportMerger merger = null;

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
