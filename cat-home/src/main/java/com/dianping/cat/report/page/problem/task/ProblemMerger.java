/**
 * 
 */
package com.dianping.cat.report.page.problem.task;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.task.TaskHelper;

public class ProblemMerger {

	private ProblemReport merge(String reportDomain, List<ProblemReport> reports) {
		ProblemReport problemReport = new ProblemReport(reportDomain);
		ProblemReportMerger merger = new HistoryProblemReportMerger(problemReport);

		for (ProblemReport report : reports) {
			report.accept(merger);
		}

		return merger.getProblemReport();
	}

	public ProblemReport mergeForDaily(String reportDomain, List<ProblemReport> reports, Set<String> domains) {
		ProblemReport report = merge(reportDomain, reports);
		Date date = report.getStartTime();
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);

		report.setStartTime(TaskHelper.todayZero(date));
		report.setEndTime(end);
		report.getDomainNames().addAll(domains);
		return report;
	}
}
