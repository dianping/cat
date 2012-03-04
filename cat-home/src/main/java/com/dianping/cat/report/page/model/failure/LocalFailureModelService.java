package com.dianping.cat.report.page.model.failure;

import java.util.List;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.failure.FailureAnalyzer;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;

public class LocalFailureModelService implements ModelService<FailureReport> {
	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

	@Override
	public ModelResponse<FailureReport> invoke(ModelRequest request) {
		FailureAnalyzer analyzer = getAnalyzer(request.getPeriod());
		ModelResponse<FailureReport> response = new ModelResponse<FailureReport>();

		if (analyzer != null) {
			List<String> domains = analyzer.getDomains();
			String d = request.getDomain();
			FailureReport report = analyzer.getReport(d != null ? d : domains.isEmpty() ? null : domains.get(0));

			if (report != null) {
				for (String domain : domains) {
					report.addDomain(domain);
				}
			}

			response.setModel(report);
		}

		return response;
	}

	private FailureAnalyzer getAnalyzer(ModelPeriod period) {
		if (period.isCurrent()) {
			return (FailureAnalyzer) m_consumer.getCurrentAnalyzer("failure");
		} else if (period.isLast()) {
			return (FailureAnalyzer) m_consumer.getLastAnalyzer("failure");
		} else {
			return null;
		}
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		ModelPeriod period = request.getPeriod();

		return period.isCurrent() || period.isLast();
	}
}
