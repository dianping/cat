package com.dianping.cat.report.page.model.event;

import java.util.List;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeEventService extends BaseCompositeModelService<EventReport> {
	public CompositeEventService() {
		super("event");
	}

	@Override
	protected BaseRemoteModelService<EventReport> createRemoteService() {
		return new RemoteEventService();
	}

	@Override
	protected EventReport merge(List<ModelResponse<EventReport>> responses) {
		EventReportMerger merger = null;

		for (ModelResponse<EventReport> response : responses) {
			if (response != null) {
				EventReport model = response.getModel();

				if (model != null) {
					if (merger == null) {
						merger = new EventReportMerger(model);
					} else {
						model.accept(merger);
					}
				}
			}
		}

		return merger == null ? null : merger.getEventReport();
	}
}
