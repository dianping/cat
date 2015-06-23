package com.dianping.cat.consumer.event;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.event.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

public class EventDelegate implements ReportDelegate<EventReport> {

	@Inject
	private TaskManager m_taskManager;

	@Inject
	private ServerFilterConfigManager m_configManager;

	@Inject
	private AllReportConfigManager m_allManager;

	private EventTpsStatisticsComputer m_computer = new EventTpsStatisticsComputer();

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
		if (reports.size() > 0) {
			EventReport all = createAggregatedReport(reports);

			reports.put(all.getDomain(), all);
		}
	}

	@Override
	public byte[] buildBinary(EventReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(EventReport report) {
		report.accept(m_computer);

		new EventReportCountFilter().visitEventReport(report);;

		return report.toString();
	}

	public EventReport createAggregatedReport(Map<String, EventReport> reports) {
		if (reports.size() > 0) {
			EventReport first = reports.values().iterator().next();
			EventReport all = makeReport(Constants.ALL, first.getStartTime().getTime(), Constants.HOUR);
			EventReportTypeAggregator visitor = new EventReportTypeAggregator(all, m_allManager);

			try {
				for (EventReport report : reports.values()) {
					String domain = report.getDomain();

					if (!domain.equals(Constants.ALL)) {
						all.getIps().add(domain);
						all.getDomainNames().add(domain);

						visitor.visitEventReport(report);
					}
				}
			} catch (Exception e) {
				Cat.logError(e);
			}
			return all;
		} else {
			return new EventReport(Constants.ALL);
		}
	}

	@Override
	public boolean createHourlyTask(EventReport report) {
		String domain = report.getDomain();

		if (domain.equals(Constants.ALL)) {
			return m_taskManager.createTask(report.getStartTime(), domain, EventAnalyzer.ID,
			      TaskProlicy.ALL_EXCLUED_HOURLY);
		} else if (m_configManager.validateDomain(domain)) {
			return m_taskManager.createTask(report.getStartTime(), report.getDomain(), EventAnalyzer.ID, TaskProlicy.ALL);
		} else {
			return true;
		}
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
	public EventReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public EventReport parseXml(String xml) throws Exception {
		EventReport report = DefaultSaxParser.parse(xml);

		return report;
	}
}
