package com.dianping.cat.report.page.model.failure;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer;
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
		FailureReportAnalyzer analyzer = getAnalyzer(request.getPeriod());
		ModelResponse<FailureReport> response = new ModelResponse<FailureReport>();

		if (analyzer != null) {
			Map<String, FailureReport> reports = analyzer.getReports();
			List<String> domains = getDomains(reports.keySet());
			String d = request.getDomain();
			FailureReport report = reports.get(d != null ? d : domains.isEmpty() ? null : domains.get(0));

			if (report != null) {
				for (String domain : domains) {
					report.addDomain(domain);
				}
			}

			response.setModel(report);
		}

		return response;
	}

	public List<String> getDomains(Set<String> keys) {
		List<String> domains = new ArrayList<String>(keys);

		Collections.sort(domains, new Comparator<String>() {
			@Override
			public int compare(String d1, String d2) {
				if (d1.equals("Cat")) {
					return 1;
				}

				return d1.compareTo(d2);
			}
		});

		return domains;
	}

	private FailureReportAnalyzer getAnalyzer(ModelPeriod period) {
		if (period.isCurrent() || period.isFuture()) {
			return (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");
		} else if (period.isLast()) {
			return (FailureReportAnalyzer) m_consumer.getLastAnalyzer("failure");
		} else {
			return null;
		}
	}

	@Override
	public boolean isEligable(ModelRequest request) {
		ModelPeriod period = request.getPeriod();

		return !period.isHistorical();
	}
}
