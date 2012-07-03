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

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public class EventMerger implements ReportMerger<EventReport> {

	@Override
	public EventReport merge(String reportDomain, List<Report> reports) {
		EventReportMerger merger = new HistoryEventReportMerger(new EventReport(reportDomain));

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
	public String mergeAll(String reportDomain, List<Report> reports, Set<String> domains) {
		EventReport eventReport = merge(reportDomain, reports);
		EventReportMerger merger = new HistoryEventReportMerger(new EventReport(reportDomain));
		EventReport eventReport2 = merge(reportDomain, reports);
		com.dianping.cat.consumer.event.model.entity.Machine allMachines = merger.mergesForAllMachine(eventReport2);
		eventReport.addMachine(allMachines);
		eventReport.getIps().add("All");
		eventReport.getDomainNames().addAll(domains);
		String content = eventReport.toString();
		return content;
	}

}
