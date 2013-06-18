package com.dianping.cat.report.page.model.cross;

import java.util.List;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeCrossService extends BaseCompositeModelService<CrossReport> {
	public CompositeCrossService() {
		super("cross");
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
