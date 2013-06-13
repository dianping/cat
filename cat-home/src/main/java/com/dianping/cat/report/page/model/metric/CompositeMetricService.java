package com.dianping.cat.report.page.model.metric;

import java.util.List;

import com.dianping.cat.consumer.metric.model.entity.MetricReport;
import com.dianping.cat.report.model.ModelRequest;
import com.dianping.cat.report.model.ModelResponse;
import com.dianping.cat.report.page.model.spi.internal.BaseCompositeModelService;
import com.dianping.cat.report.page.model.spi.internal.BaseRemoteModelService;

public class CompositeMetricService extends BaseCompositeModelService<MetricReport> {
	public CompositeMetricService() {
		super("metric");
	}

	@Override
	protected BaseRemoteModelService<MetricReport> createRemoteService() {
		return new RemoteMetricService();
	}

	@Override
	protected MetricReport merge(ModelRequest request, List<ModelResponse<MetricReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		MetricReportMerger merger = new MetricReportMerger(new MetricReport(request.getDomain()));

		for (ModelResponse<MetricReport> response : responses) {
			MetricReport model = response.getModel();
			if (model != null) {
				model.accept(merger);
			}
		}

		return merger.getMetricReport();
	}
}
