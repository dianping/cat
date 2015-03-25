package com.dianping.cat.report.page.heartbeat.service;

import java.util.List;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

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
