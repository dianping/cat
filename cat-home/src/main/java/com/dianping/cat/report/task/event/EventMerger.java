/**
 * 
 */
package com.dianping.cat.report.task.event;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.dal.report.Report;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.event.EventReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportMerger;

public class EventMerger implements ReportMerger<EventReport> {

	private EventReport merge(String reportDomain, List<Report> reports, boolean isDaily) {
		EventReportMerger merger = null;

		if (isDaily) {
			merger = new HistoryEventReportMerger(new EventReport(reportDomain));
		} else {
			merger = new EventReportMerger(new EventReport(reportDomain));
		}
		for (Report report : reports) {
			String xml = report.getContent();
			EventReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		EventReport eventReport = merger.getEventReport();
		return eventReport;
	}

	@Override
	public EventReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		EventReport eventReport = merge(reportDomain, reports, true);
		HistoryEventReportMerger merger = new HistoryEventReportMerger(new EventReport(reportDomain));
		EventReport eventReport2 = merge(reportDomain, reports, true);
		Machine allMachines = merger.mergesForAllMachine(eventReport2);

		eventReport.addMachine(allMachines);
		eventReport.getIps().add("All");
		eventReport.getDomainNames().addAll(domains);

		Date date = eventReport.getStartTime();
		eventReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		eventReport.setEndTime(end);
		return eventReport;
	}

	@Override
	public EventReport mergeForGraph(String reportDomain, List<Report> reports) {
		EventReport eventReport = merge(reportDomain, reports, false);
		EventReportMerger merger = new EventReportMerger(new EventReport(reportDomain));
		EventReport eventReport2 = merge(reportDomain, reports, false);

		Machine allMachines = merger.mergesForAllMachine(eventReport2);
		eventReport.addMachine(allMachines);
		eventReport.getIps().add("All");
		return eventReport;
	}
}
