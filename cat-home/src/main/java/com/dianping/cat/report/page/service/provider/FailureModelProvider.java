package com.dianping.cat.report.page.service.provider;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.RealtimeConsumer;
import com.dianping.cat.consumer.failure.FailureReportAnalyzer;
import com.dianping.cat.consumer.failure.model.entity.FailureReport;
import com.dianping.cat.consumer.failure.model.entity.Segment;
import com.dianping.cat.consumer.failure.model.entity.Threads;
import com.dianping.cat.consumer.failure.model.transform.DefaultXmlBuilder;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.report.tool.Constants;
import com.dianping.cat.report.tool.DateUtils;
import com.site.lookup.annotation.Inject;

public class FailureModelProvider implements ModelProvider {

	@Inject(type = MessageConsumer.class, value = "realtime")
	private RealtimeConsumer m_consumer;

	@Override
	public List<String> getDomains() {
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");
		List<String> domains = analyzer.getAllDomains();
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
		String index = parameters.get("index");
		if (index == null) {
			index = Constants.MEMORY_CURRENT;
		}
		String domain = parameters.get("domain");
		String ip = parameters.get("ip");
		FailureReportAnalyzer analyzer = null;

		int pos = Constants.LAST;
		if (index.equals(Constants.MEMORY_CURRENT)) {
			analyzer = (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");
			pos = Constants.CURRENT;
		} else if (index.equals(Constants.MEMORY_LAST)) {
			analyzer = (FailureReportAnalyzer) m_consumer.getLastAnalyzer("failure");
			pos = Constants.LAST;
		}
		String xmlResult = "";
		if (analyzer == null) {
			xmlResult = getFailureDataByNew(pos, domain, ip);
		} else {
			xmlResult = getFailureDataFromMemory(analyzer, domain, ip);
		}
		return xmlResult;
	}

	public List<String> getIpsByDomain(String domain) {
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");
		List<String> ips = analyzer.getHostIpByDomain(domain);
		Collections.sort(ips);
		return ips;
	}

	private String getFailureXMLData(FailureReport report) {
		return new DefaultXmlBuilder().buildXml(report);
	}

	private String getFailureDataFromMemory(FailureReportAnalyzer analyzer, String domain, String ip) {
		FailureReport report = analyzer.generateByDomainAndIp(domain, ip);
		return getFailureXMLData(report);
	}

	/*
	 * private String getFailureDataFromFile(String basePath, String file) {
	 * String result = ""; try { result = Files.forIO().readFrom(new
	 * File(basePath + file), "utf-8"); result =
	 * result.substring(result.indexOf("<body>") + 6, result.indexOf("</body>"));
	 * } catch (IOException e) { e.printStackTrace(); } return result; }
	 */

	private String getFailureDataByNew(int pos, String domain, String ip) {
		long currentTime = System.currentTimeMillis();
		long currentStart = currentTime - currentTime % DateUtils.HOUR;
		long lastStart = currentTime - currentTime % DateUtils.HOUR - DateUtils.HOUR;
		Date date = new Date();
		if (pos == Constants.CURRENT) {
			date.setTime(currentStart);
		} else {
			date.setTime(lastStart);
		}
		FailureReport report = new FailureReport();

		report.setMachine(ip);
		report.setThreads(new Threads());
		report.setStartTime(date);
		report.setEndTime(new Date(date.getTime() + DateUtils.HOUR - DateUtils.MINUTE));
		report.setDomain(domain);
		long start = report.getStartTime().getTime();
		long endTime = report.getEndTime().getTime();
		Map<String, Segment> segments = report.getSegments();
		for (; start <= endTime; start = start + 60 * 1000) {
			String minute = DateUtils.SDF_SEG.format(new Date(start));
			segments.put(minute, new Segment(minute));
		}
		return getFailureXMLData(report);
	}

	@Override
	public String getDefaultDomain() {
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");
		List<String> domains = analyzer.getAllDomains();
		Collections.sort(domains);
		if (domains != null && domains.size() > 0) {
			return domains.get(0);
		}
		return null;
	}
	
	public String getDefaultIpByDomain(String domain){
		FailureReportAnalyzer analyzer = (FailureReportAnalyzer) m_consumer.getCurrentAnalyzer("failure");
		List<String> ips = analyzer.getHostIpByDomain(domain);
		if(ips!=null&&ips.size()>0){
			return ips.get(0);
		}
		return null;
	}
}
