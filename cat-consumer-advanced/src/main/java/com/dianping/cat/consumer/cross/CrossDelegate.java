package com.dianping.cat.consumer.cross;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.cross.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.cross.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class CrossDelegate implements ReportDelegate<CrossReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, CrossReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, CrossReport> reports) {
		for (CrossReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());
		}
	}

	@Override
	public byte[] buildBinary(CrossReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(CrossReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(CrossReport report) {
		return m_taskManager.createTask(report.getStartTime(), report.getDomain(), CrossAnalyzer.ID,
		      TaskProlicy.ALL_EXCLUED_HOURLY);
	}

	@Override
	public String getDomain(CrossReport report) {
		return report.getDomain();
	}

	@Override
	public CrossReport makeReport(String domain, long startTime, long duration) {
		CrossReport report = new CrossReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public CrossReport mergeReport(CrossReport old, CrossReport other) {
		CrossReportMerger merger = new CrossReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public CrossReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public CrossReport parseXml(String xml) throws Exception {
		CrossReport report = DefaultSaxParser.parse(xml);

		return report;
	}
}
