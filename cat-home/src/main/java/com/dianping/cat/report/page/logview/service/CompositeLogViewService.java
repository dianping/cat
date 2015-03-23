package com.dianping.cat.report.page.logview.service;

import java.util.List;

import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeLogViewService extends BaseCompositeModelService<String> {
	public CompositeLogViewService() {
		super("logview");
	}

	@Override
	protected BaseRemoteModelService<String> createRemoteService() {
		RemoteLogViewService service = new RemoteLogViewService();
		
		service.setManager(m_configManager);
		return  service;
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
