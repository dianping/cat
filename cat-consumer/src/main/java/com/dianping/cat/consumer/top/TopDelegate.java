package com.dianping.cat.consumer.top;

import java.util.Date;
import java.util.Map;

import com.dianping.cat.consumer.top.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.top.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;

public class TopDelegate implements ReportDelegate<TopReport> {

	@Override
	public void afterLoad(Map<String, TopReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, TopReport> reports) {
	}

	@Override
	public byte[] buildBinary(TopReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(TopReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(TopReport report) {
		return true;
	}

	@Override
	public String getDomain(TopReport report) {
		return report.getDomain();
	}

	@Override
	public TopReport makeReport(String domain, long startTime, long duration) {
		TopReport report = new TopReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public TopReport mergeReport(TopReport old, TopReport other) {
		TopReportMerger merger = new TopReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public TopReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public TopReport parseXml(String xml) throws Exception {
		TopReport report = DefaultSaxParser.parse(xml);

		return report;
	}
}
