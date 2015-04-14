/**
 * 
 */
package com.dianping.cat.report.page.event.task;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.event.EventReportCountFilter;
import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.report.task.TaskHelper;

public class EventMerger {

	private EventReport merge(String reportDomain, List<EventReport> reports, double duration) {
		EventReportMerger merger = new HistoryEventReportMerger(new EventReport(reportDomain)).setDuration(duration);

		for (EventReport report : reports) {
			report.accept(merger);
		}
		return merger.getEventReport();
	}

	public EventReport mergeForDaily(String reportDomain, List<EventReport> reports, Set<String> domains, double duration) {
		EventReport eventReport = merge(reportDomain, reports, duration);
		HistoryEventReportMerger merger = new HistoryEventReportMerger(new EventReport(reportDomain));
		EventReport eventReport2 = merge(reportDomain, reports, duration);
		Machine allMachines = merger.mergesForAllMachine(eventReport2);

		eventReport.addMachine(allMachines);
		eventReport.getIps().add("All");
		eventReport.getDomainNames().addAll(domains);

		Date date = eventReport.getStartTime();
		eventReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		eventReport.setEndTime(end);

		new EventReportCountFilter().visitEventReport(eventReport);
		return eventReport;
	}

}
