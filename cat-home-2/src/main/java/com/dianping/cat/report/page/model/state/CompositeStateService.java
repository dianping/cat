package com.dianping.cat.report.page.model.state;

import java.util.List;

import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateReportMerger;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class CompositeStateService extends BaseCompositeModelService<StateReport> {
	public CompositeStateService() {
		super(StateAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<StateReport> createRemoteService() {
		return new RemoteStateService();
	}

	@Override
	protected StateReport merge(ModelRequest request, List<ModelResponse<StateReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		StateReportMerger merger = new StateReportMerger(new StateReport(request.getDomain()));
		for (ModelResponse<StateReport> response : responses) {
			StateReport model = response.getModel();
			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getStateReport();
	}
}
