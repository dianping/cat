package com.dianping.cat.report.page.business.service;

import java.util.List;

import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.BusinessReportMerger;
import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeBusinessService extends BaseCompositeModelService<BusinessReport> {

	public CompositeBusinessService() {
		super(BusinessAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<BusinessReport> createRemoteService() {
		return new RemoteBusinessService();
	}

	@Override
	protected BusinessReport merge(ModelRequest request, List<ModelResponse<BusinessReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}

		BusinessReportMerger merger = new BusinessReportMerger(new BusinessReport(request.getDomain()));

		for (ModelResponse<BusinessReport> response : responses) {
			BusinessReport model = response.getModel();

			if (model != null) {
				model.accept(merger);
			}
		}
		return merger.getBusinessReport();
	}

}
