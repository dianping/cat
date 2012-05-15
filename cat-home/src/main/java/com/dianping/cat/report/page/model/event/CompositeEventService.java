package com.dianping.cat.report.page.model.event;

import java.util.List;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.helper.CatString;
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
		if (responses.size() == 0) {
			return null;
		}
		EventReportMerger merger = new EventReportMerger(new EventReport(request.getDomain()));
		String ip = request.getProperty("ip");
		if (ip.equals(CatString.ALL_IP)) {
			merger.setAllIp(true);
		}
		for (ModelResponse<EventReport> response : responses) {
			EventReport model = response.getModel();
			model.accept(merger);
		}

		EventReport report = merger.getEventReport();
		String all = request.getProperty("all");

		if ("true".equals(all)) {
			String type = request.getProperty("type");
			EventNameAggregator aggregator = new EventNameAggregator(report);
			EventName n = aggregator.mergesFor(type, ip);
			EventType t = new EventType(type).addName(n);
			EventReport result = new EventReport(request.getDomain());
			result.findOrCreateMachine(ip).addType(t);

			return result;
		} else {
			return report;
		}
	}
}
