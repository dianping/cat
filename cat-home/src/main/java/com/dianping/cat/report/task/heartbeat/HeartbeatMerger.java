/**
 * 
 */
package com.dianping.cat.report.task.heartbeat;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.report.page.model.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.report.task.ReportMerger;

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public class HeartbeatMerger implements ReportMerger<HeartbeatReport> {

	public HeartbeatReport mergeForGraph(String reportDomain, List<Report> reports) {
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			HeartbeatReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();
		return heartbeatReport;
	}

	public HeartbeatReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		return null;
	}
}
