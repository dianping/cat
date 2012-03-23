package com.dianping.cat.report.page.model.ip;

import java.util.List;

import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeIpService extends BaseCompositeModelService<IpReport> {
	public CompositeIpService() {
		super("ip");
	}

	@Override
	protected BaseRemoteModelService<IpReport> createRemoteService() {
		return new RemoteIpService();
	}

	@Override
	protected IpReport merge(List<ModelResponse<IpReport>> responses) {
		IpReportMerger merger = null;

		for (ModelResponse<IpReport> response : responses) {
			if (response != null) {
				IpReport model = response.getModel();

				if (model != null) {
					if (merger == null) {
						merger = new IpReportMerger(model);
					} else {
						model.accept(merger);
					}
				}
			}
		}

		return merger == null ? null : merger.getIpReport();
	}
}
