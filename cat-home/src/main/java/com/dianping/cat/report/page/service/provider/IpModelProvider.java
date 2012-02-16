package com.dianping.cat.report.page.service.provider;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.ip.IpAnalyzer;
import com.dianping.cat.consumer.ip.model.entity.IpReport;
import com.dianping.cat.consumer.ip.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.tool.Constant;
import com.site.lookup.annotation.Inject;

public class IpModelProvider implements ModelProvider {
	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

	@Override
	public String getDefaultDomain() {
		IpAnalyzer analyzer = (IpAnalyzer) m_consumer.getCurrentAnalyzer("ip");
		List<String> domains = analyzer.getDomains();
		Collections.sort(domains);
		if (domains != null && domains.size() > 0) {
			return domains.get(0);
		}
		return null;
	}

	@Override
	public List<String> getDomains() {
		IpAnalyzer analyzer = (IpAnalyzer) m_consumer.getCurrentAnalyzer("ip");
		List<String> domains = analyzer.getDomains();
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
		IpAnalyzer analyzer = null;
		
		if (index == null) {
			index = Constant.MEMORY_CURRENT;
		}
		if (index.equals(Constant.MEMORY_CURRENT)) {
			analyzer = (IpAnalyzer) m_consumer.getCurrentAnalyzer("ip");
		} else if (index.equals(Constant.MEMORY_LAST)) {
			analyzer = (IpAnalyzer) m_consumer.getLastAnalyzer("ip");
		}
		
		IpReport report = analyzer.generate(domain);
		return new DefaultXmlBuilder().buildXml(report);
	}

}
