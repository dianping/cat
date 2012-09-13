/**
 * 
 */
package com.dianping.cat.report.task.heartbeat;

import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.report.page.model.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.report.task.spi.ReportMerger;

public class HeartbeatMerger implements ReportMerger<HeartbeatReport> {

	public HeartbeatReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		throw new RuntimeException("HeartbeatReport cat't be merged for daily report!");
	}

	public HeartbeatReport mergeForGraph(String reportDomain, List<Report> reports) {
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			HeartbeatReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		HeartbeatReport heartbeatReport = merger.getHeartbeatReport();
		return heartbeatReport;
	}
}
