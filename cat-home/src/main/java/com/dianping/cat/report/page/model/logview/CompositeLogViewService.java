package com.dianping.cat.report.page.model.logview;

import java.util.List;

import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeLogViewService extends BaseCompositeModelService<String> {
	public CompositeLogViewService() {
		super("logview");
	}

	@Override
	protected BaseRemoteModelService<String> createRemoteService() {
		return new RemoteLogViewService();
	}

	@Override
	protected String merge(ModelRequest request, List<ModelResponse<String>> responses) {
		for (ModelResponse<String> response : responses) {
			if (response != null) {
				String model = response.getModel();

				if (model != null) {
					return model;
				}
			}
		}

		return null;
	}
}
