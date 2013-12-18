package com.dianping.cat.consumer.browser;

import java.util.Date;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.browsermeta.model.entity.BrowserMetaReport;
import com.dianping.cat.consumer.browsermeta.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.browsermeta.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.browsermeta.model.transform.DefaultSaxParser;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class BrowserMetaDelegate implements ReportDelegate<BrowserMetaReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, BrowserMetaReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, BrowserMetaReport> reports) {
	}

	@Override
	public String buildXml(BrowserMetaReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(BrowserMetaReport report) {
		return m_taskManager.createTask(report.getStartTime(), report.getDomain(), BrowserMetaAnalyzer.ID,
		      TaskProlicy.HOULY);
	}

	@Override
	public String getDomain(BrowserMetaReport report) {
		return report.getDomain();
	}

	@Override
	public BrowserMetaReport makeReport(String domain, long startTime, long duration) {
		BrowserMetaReport report = new BrowserMetaReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public BrowserMetaReport mergeReport(BrowserMetaReport old, BrowserMetaReport other) {
		BrowserMetaReportMerger merger = new BrowserMetaReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public BrowserMetaReport parseXml(String xml) throws Exception {
		BrowserMetaReport report = DefaultSaxParser.parse(xml);

		return report;
	}

	@Override
	public byte[] buildBinary(BrowserMetaReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public BrowserMetaReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}
}
