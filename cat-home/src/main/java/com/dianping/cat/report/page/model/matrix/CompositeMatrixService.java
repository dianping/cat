package com.dianping.cat.report.page.model.matrix;

import java.util.List;

import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeMatrixService extends BaseCompositeModelService<MatrixReport> {
	public CompositeMatrixService() {
		super("matrix");
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
