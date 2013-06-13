package com.dianping.cat.report.task.database;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.core.dal.Report;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.database.model.entity.DatabaseReport;
import com.dianping.cat.consumer.database.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.page.model.database.DatabaseReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportMerger;

public class DatabaseMerger implements ReportMerger<DatabaseReport> {

	private DatabaseReport getDailyReport(String reportDatabase, List<Report> reports, boolean allDomain) {
		DatabaseReportMerger merger = new DatabaseReportMerger(new DatabaseReport(reportDatabase));
		if (allDomain) {
			merger.setAllDomain(true);
		}
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
		return databaseReport;
	}

	@Override
	public DatabaseReport mergeForDaily(String reportDatabase, List<Report> reports, Set<String> databaseNames) {
		DatabaseReport databaseReport = getDailyReport(reportDatabase, reports, false);
		DatabaseReport databaseReport2 = getDailyReport(reportDatabase, reports, true);

		databaseReport.addDomain(databaseReport2.findDomain(CatString.ALL));
		databaseReport.getDomainNames().add(CatString.ALL);

		Date date = databaseReport.getStartTime();
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);

		databaseReport.getDatabaseNames().addAll(databaseNames);
		databaseReport.setStartTime(TaskHelper.todayZero(date));
		databaseReport.setEndTime(end);
		return databaseReport;
	}

	@Override
	public DatabaseReport mergeForGraph(String reportDomain, List<Report> reports) {
		throw new RuntimeException("Database report don't need graph!");
	}
}
