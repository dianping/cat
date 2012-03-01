package com.dianping.cat.report.page.service.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.transaction.TransactionReportAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.tool.Constants;
import com.site.lookup.annotation.Inject;

public class TransactionModelProvider implements ModelProvider {

	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;
	
	@Override
	public List<String> getDomains() {
		TransactionReportAnalyzer analyzer = (TransactionReportAnalyzer) m_consumer.getCurrentAnalyzer("transaction");
		List<String> domains = new ArrayList<String>(analyzer.getReports().keySet());
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

	@Override
	public String getModel(Map<String, String> parameters) {
		String domain = parameters.get("domain");
		String index = parameters.get("index");
		TransactionReportAnalyzer analyzer = null;
		
		if (index == null) {
			index = Constants.MEMORY_CURRENT;
		}
		if (index.equals(Constants.MEMORY_CURRENT)) {
			analyzer = (TransactionReportAnalyzer) m_consumer.getCurrentAnalyzer("transaction");
		} else if (index.equals(Constants.MEMORY_LAST)) {
			analyzer = (TransactionReportAnalyzer) m_consumer.getLastAnalyzer("transaction");
		}
		TransactionReport report;
		
		if (analyzer == null) {
			report = new TransactionReport(domain);
		} else {
			report = analyzer.generate(domain);
		}
		return new DefaultXmlBuilder().buildXml(report);
	}

	@Override
   public String getDefaultDomain() {
		TransactionReportAnalyzer analyzer = (TransactionReportAnalyzer) m_consumer.getCurrentAnalyzer("transaction");
		List<String> domains = new ArrayList<String>(analyzer.getReports().keySet());
		Collections.sort(domains);
		if (domains != null && domains.size() > 0) {
			return domains.get(0);
		}
		return null;
   }
}
