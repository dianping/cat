package com.dianping.cat.report.task.database;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.report.page.model.database.DatabaseReportMerger;
import com.dianping.cat.report.task.ReportMerger;
import com.dianping.cat.report.task.TaskHelper;

public class DatabaseMerger implements ReportMerger<DatabaseReport> {

	@Override
	public DatabaseReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		DatabaseReportMerger merger = new DatabaseReportMerger(new DatabaseReport(reportDomain));
		
		for (Report report : reports) {
			String xml = report.getContent();
			try {
				DatabaseReport model = DefaultSaxParser.parse(xml);
		
				model.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		DatabaseReport databaseReport = merger.getDatabaseReport();
		Date date = databaseReport.getStartTime();
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		
		databaseReport.getDatabaseNames().addAll(domains);
		databaseReport.setStartTime(TaskHelper.todayZero(date));
		databaseReport.setEndTime(end);
		return databaseReport;
	}

	@Override
	public DatabaseReport mergeForGraph(String reportDomain, List<Report> reports) {
		throw new RuntimeException("Database report don't need graph!");
	}
}
