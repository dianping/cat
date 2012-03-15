package com.dianping.cat.report.page.model.ip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.model.entity.AllDomains;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;

public class LocalIpService implements ModelService<ProblemReport> {
	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

	@Override
	public ModelResponse<ProblemReport> invoke(ModelRequest request) {
		ProblemAnalyzer analyzer = getAnalyzer(request.getPeriod());
		ModelResponse<ProblemReport> response = new ModelResponse<ProblemReport>();

		if (analyzer != null) {
			Map<String, ProblemReport> reports = analyzer.getReports();
			List<String> domains = getDomains(reports.keySet());
			String d = request.getDomain();
			String domain = d != null && d.length() > 0 ? d : domains.isEmpty() ? null : domains.get(0);
			ProblemReport report = reports.get(domain);

			if (report != null) {
				AllDomains allDomains = new AllDomains();

				allDomains.getDomains().addAll(domains);
				report.setAllDomains(allDomains);
			}

			response.setModel(report);
		}

		return response;
	}

	List<String> getDomains(Set<String> keys) {
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

	private ProblemAnalyzer getAnalyzer(ModelPeriod period) {
		if (period.isCurrent() || period.isFuture()) {
			return (ProblemAnalyzer) m_consumer.getCurrentAnalyzer("problem");
		} else if (period.isLast()) {
			return (ProblemAnalyzer) m_consumer.getLastAnalyzer("problem");
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
