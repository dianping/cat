package com.dianping.cat.report.page.model.heartbeat;

import java.util.List;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeHeartbeatService extends BaseCompositeModelService<HeartbeatReport> {
	public CompositeHeartbeatService() {
		super("heartbeat");
	}

	@Override
	protected BaseRemoteModelService<HeartbeatReport> createRemoteService() {
		return new RemoteHeartbeatService();
	}

	@Override
	protected HeartbeatReport merge(ModelRequest request, List<ModelResponse<HeartbeatReport>> responses) {
		HeartbeatReportMerger merger = null;

		for (ModelResponse<HeartbeatReport> response : responses) {
			if (response != null) {
				HeartbeatReport model = response.getModel();

				if (model != null) {
					if (merger == null) {
						merger = new HeartbeatReportMerger(model);
					} else {
						model.accept(merger);
					}
				}
			}
		}

		return merger == null ? null : merger.getHeartbeatReport();
	}
}
