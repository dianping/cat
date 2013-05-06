package com.dianping.cat.report.task.sql;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dainping.cat.consumer.core.dal.Report;
import com.dianping.cat.Cat;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.page.model.sql.SqlReportMerger;
import com.dianping.cat.report.task.TaskHelper;
import com.dianping.cat.report.task.spi.ReportMerger;

public class SqlMerger implements ReportMerger<SqlReport> {

	private SqlReport getDailyReport(List<Report> reports, String reportDomain, boolean allDatabase) {
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(reportDomain));
		if (allDatabase) {
			merger.setAllDatabase(true);
		}
		for (Report report : reports) {
			String xml = report.getContent();
			try {
				SqlReport model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		SqlReport sqlReport = merger.getSqlReport();
		return sqlReport;
	}

	@Override
	public SqlReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		SqlReport sqlReport = getDailyReport(reports, reportDomain, false);
		SqlReport sqlReport2 = getDailyReport(reports, reportDomain, true);

		sqlReport.addDatabase(sqlReport2.findOrCreateDatabase(CatString.ALL));
		sqlReport.getDomainNames().add(CatString.ALL);
		sqlReport.getDomainNames().addAll(domains);

		Date date = sqlReport.getStartTime();
		sqlReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		sqlReport.setEndTime(end);
		return sqlReport;
	}

	@Override
	public SqlReport mergeForGraph(String reportDomain, List<Report> reports) {
		throw new RuntimeException("Sql report don't need graph!");
	}
}
