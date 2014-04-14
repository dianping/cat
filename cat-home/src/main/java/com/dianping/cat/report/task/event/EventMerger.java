/**
 * 
 */
package com.dianping.cat.report.task.event;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.report.task.TaskHelper;

public class EventMerger {

	private EventReport merge(String reportDomain, List<EventReport> reports, boolean isDaily) {
		EventReportMerger merger = null;

		if (isDaily) {
			merger = new HistoryEventReportMerger(new EventReport(reportDomain));
		} else {
			merger = new EventReportMerger(new EventReport(reportDomain));
		}
		for (EventReport report : reports) {
			report.accept(merger);
		}
		return merger.getEventReport();
	}

	public EventReport mergeForDaily(String reportDomain, List<EventReport> reports, Set<String> domains) {
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

}
