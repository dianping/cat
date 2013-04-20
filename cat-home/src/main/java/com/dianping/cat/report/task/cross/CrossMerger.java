package com.dianping.cat.report.task.cross;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.core.dal.Report;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.report.page.model.cross.CrossReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportMerger;

public class CrossMerger implements ReportMerger<CrossReport> {

	@Override
	public CrossReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		CrossReportMerger merger = new CrossReportMerger(new CrossReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			try {
				CrossReport model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		CrossReport crossReport = merger.getCrossReport();

		crossReport.getDomainNames().addAll(domains);
		Date date = crossReport.getStartTime();
		crossReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		crossReport.setEndTime(end);
		return crossReport;
	}

	@Override
	public CrossReport mergeForGraph(String reportDomain, List<Report> reports) {
		throw new RuntimeException("Cross report don't need graph!");
	}
}
