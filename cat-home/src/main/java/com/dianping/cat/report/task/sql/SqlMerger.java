package com.dianping.cat.report.task.sql;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.sql.SqlReportMerger;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.report.task.TaskHelper;

public class SqlMerger {

	private SqlReport buildDailyReport(List<SqlReport> reports, String reportDomain, boolean allDatabase) {
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(reportDomain));
		if (allDatabase) {
			merger.setAllDatabase(true);
		}
		for (SqlReport report : reports) {
			report.accept(merger);
		}

		return merger.getSqlReport();
	}

	public SqlReport mergeForDaily(String reportDomain, List<SqlReport> reports, Set<String> domains) {
		SqlReport sqlReport = buildDailyReport(reports, reportDomain, false);
		SqlReport sqlReport2 = buildDailyReport(reports, reportDomain, true);

		sqlReport.addDatabase(sqlReport2.findOrCreateDatabase(Constants.ALL));
		sqlReport.getDomainNames().add(Constants.ALL);
		sqlReport.getDomainNames().addAll(domains);

		Date date = sqlReport.getStartTime();
		sqlReport.setStartTime(TaskHelper.todayZero(date));
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);
		sqlReport.setEndTime(end);
		return sqlReport;
	}

}
