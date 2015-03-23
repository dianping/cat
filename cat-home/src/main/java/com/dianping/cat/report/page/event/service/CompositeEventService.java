package com.dianping.cat.report.page.event.service;

import java.util.List;

import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeEventService extends BaseCompositeModelService<EventReport> {
	public CompositeEventService() {
		super(EventAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<EventReport> createRemoteService() {
		return new RemoteEventService();
	}

	@Override
	protected EventReport merge(ModelRequest request, List<ModelResponse<EventReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		EventReportMerger merger = new EventReportMerger(new EventReport(request.getDomain()));
		for (ModelResponse<EventReport> response : responses) {
			EventReport model = response.getModel();
			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getEventReport();
	}
}
