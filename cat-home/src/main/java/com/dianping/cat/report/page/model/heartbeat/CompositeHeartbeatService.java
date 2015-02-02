package com.dianping.cat.report.page.model.heartbeat;

import java.util.List;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class CompositeHeartbeatService extends BaseCompositeModelService<HeartbeatReport> {
	public CompositeHeartbeatService() {
		super(HeartbeatAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<HeartbeatReport> createRemoteService() {
		return new RemoteHeartbeatService();
	}

	@Override
	protected HeartbeatReport merge(ModelRequest request, List<ModelResponse<HeartbeatReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(request.getDomain()));

		for (ModelResponse<HeartbeatReport> response : responses) {
			if (response != null) {
				HeartbeatReport model = response.getModel();

				if (model != null) {
					model.accept(merger);
				}
			}
		}
		return merger.getHeartbeatReport();
	}
}
