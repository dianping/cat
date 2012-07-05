/**
 * 
 */
package com.dianping.cat.report.task;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.report.page.model.event.EventReportMerger;

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
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
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
		com.dianping.cat.consumer.event.model.entity.Machine allMachines = merger.mergesForAllMachine(eventReport2);
		eventReport.addMachine(allMachines);
		eventReport.getIps().add("All");
		eventReport.getDomainNames().addAll(domains);
		return eventReport;
	}

	@Override
	public EventReport mergeForGraph(String reportDomain, List<Report> reports) {
		EventReport eventReport = merge(reportDomain, reports, false);
		EventReportMerger merger = new EventReportMerger(new EventReport(reportDomain));
		EventReport eventReport2 = merge(reportDomain, reports, false);
		com.dianping.cat.consumer.event.model.entity.Machine allMachines = merger.mergesForAllMachine(eventReport2);
		eventReport.addMachine(allMachines);
		eventReport.getIps().add("All");
		return eventReport;
	}
}
