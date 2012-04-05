package com.dianping.cat.report.page.model.event;

import java.util.List;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.report.page.model.spi.ModelRequest;
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
	protected EventReport merge(ModelRequest request, List<ModelResponse<EventReport>> responses) {
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

		if (merger == null) {
			return null;
		} else {
			EventReport report = merger.getEventReport();
			String all = request.getProperty("all");

			if ("true".equals(all)) {
				String type = request.getProperty("type");
				EventNameAggregator aggregator = new EventNameAggregator(report);
				EventName n = aggregator.mergesFor(type);
				EventType t = new EventType(type).addName(n);
				EventReport result = new EventReport(request.getDomain()).addType(t);

				return result;
			} else {
				return report;
			}
		}
	}
}
