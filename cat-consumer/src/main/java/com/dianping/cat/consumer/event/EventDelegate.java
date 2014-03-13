package com.dianping.cat.consumer.event;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.service.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class EventDelegate implements ReportDelegate<EventReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, EventReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, EventReport> reports) {
		for (EventReport report : reports.values()) {
			Set<String> domainNames = report.getDomainNames();

			domainNames.clear();
			domainNames.addAll(reports.keySet());
		}
	}

	@Override
	public String buildXml(EventReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(EventReport report) {
		return m_taskManager.createTask(report.getStartTime(), report.getDomain(), EventAnalyzer.ID, TaskProlicy.ALL);
	}

	@Override
	public String getDomain(EventReport report) {
		return report.getDomain();
	}

	@Override
	public EventReport makeReport(String domain, long startTime, long duration) {
		EventReport report = new EventReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public EventReport mergeReport(EventReport old, EventReport other) {
		EventReportMerger merger = new EventReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public EventReport parseXml(String xml) throws Exception {
		EventReport report = DefaultSaxParser.parse(xml);

		return report;
	}

	@Override
   public byte[] buildBinary(EventReport report) {
		return DefaultNativeBuilder.build(report);
   }

	@Override
   public EventReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
   }
}
