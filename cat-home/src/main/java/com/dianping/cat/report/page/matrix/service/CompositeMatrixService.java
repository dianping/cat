package com.dianping.cat.report.page.matrix.service;

import java.util.List;

import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixReportMerger;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeMatrixService extends BaseCompositeModelService<MatrixReport> {
	public CompositeMatrixService() {
		super(MatrixAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<MatrixReport> createRemoteService() {
		return new RemoteMatrixService();
	}

	@Override
	protected MatrixReport merge(ModelRequest request, List<ModelResponse<MatrixReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		MatrixReportMerger merger = new MatrixReportMerger(new MatrixReport(request.getDomain()));
		for (ModelResponse<MatrixReport> response : responses) {
			MatrixReport model = response.getModel();
			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getMatrixReport();
	}
}
