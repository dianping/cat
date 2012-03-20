package com.dianping.cat.report.page.model.ip;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.ip.IpAnalyzer;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.entity.AllDomains;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.dianping.cat.report.page.model.spi.ModelRequest;
import com.dianping.cat.report.page.model.spi.ModelResponse;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.site.lookup.annotation.Inject;

public class LocalIpService implements ModelService<IpReport> {
	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

	@Override
	public ModelResponse<IpReport> invoke(ModelRequest request) {
		IpAnalyzer analyzer = getAnalyzer(request.getPeriod());
		ModelResponse<IpReport> response = new ModelResponse<IpReport>();

		if (analyzer != null) {
			Map<String, IpReport> reports = analyzer.getReports();
			List<String> domains = getDomains(reports.keySet());
			String d = request.getDomain();
			String domain = d != null && d.length() > 0 ? d : domains.isEmpty() ? null : domains.get(0);
			IpReport report = reports.get(domain);

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

	private IpAnalyzer getAnalyzer(ModelPeriod period) {
		if (period.isCurrent() || period.isFuture()) {
			return (IpAnalyzer) m_consumer.getCurrentAnalyzer("ip");
		} else if (period.isLast()) {
			return (IpAnalyzer) m_consumer.getLastAnalyzer("ip");
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
