package com.dianping.cat.report.task.sql;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.sql.model.entity.SqlReport;
import com.dianping.cat.consumer.sql.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.report.page.model.sql.SqlReportMerger;
import com.dianping.cat.report.task.ReportMerger;
import com.dianping.cat.report.task.TaskHelper;

public class SqlMerger implements ReportMerger<SqlReport> {
	
	@Override
   public SqlReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		SqlReportMerger merger = new SqlReportMerger(new SqlReport(reportDomain));
		for (Report report : reports) {
			String xml = report.getContent();
			SqlReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}
		SqlReport sqlReport = merger.getSqlReport();
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
