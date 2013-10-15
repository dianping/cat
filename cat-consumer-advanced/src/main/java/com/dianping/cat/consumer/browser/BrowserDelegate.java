package com.dianping.cat.consumer.browser;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.browser.BrowserReportMerger;
import com.dianping.cat.consumer.browser.model.entity.BrowserReport;
import com.dianping.cat.consumer.browser.model.transform.DefaultSaxParser;
import com.dianping.cat.consumer.browser.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.browser.model.transform.DefaultNativeParser;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class BrowserDelegate implements ReportDelegate<BrowserReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, BrowserReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, BrowserReport> reports) {
		for (BrowserReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());
		}
	}

	@Override
	public String buildXml(BrowserReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(BrowserReport report) {
		return m_taskManager.createTask(report.getStartTime(), report.getDomain(), BrowserAnalyzer.ID, TaskProlicy.ALL_EXCLUED_HOURLY);
	}

	@Override
	public String getDomain(BrowserReport report) {
		return report.getDomain();
	}

	@Override
	public BrowserReport makeReport(String domain, long startTime, long duration) {
		BrowserReport report = new BrowserReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public BrowserReport mergeReport(BrowserReport old, BrowserReport other) {
		BrowserReportMerger merger = new BrowserReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public BrowserReport parseXml(String xml) throws Exception {
		BrowserReport report = DefaultSaxParser.parse(xml);

		return report;
	}
	
	@Override
	public byte[] buildBinary(BrowserReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public BrowserReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}
}
