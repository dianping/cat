/**
 * 
 */
package com.dianping.cat.report.task.problem;

import java.util.Date;
import java.util.List;
import java.util.Set;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.task.TaskHelper;

public class ProblemMerger  {

	private ProblemReport merge(String reportDomain, List<ProblemReport> reports, boolean isDaily) {
		ProblemReportMerger merger = null;
		
		if (isDaily) {
			merger = new HistoryProblemReportMerger(new ProblemReport(reportDomain));
		} else {
			merger = new ProblemReportMerger(new ProblemReport(reportDomain));
		}
		for (ProblemReport report : reports) {
				report.accept(merger);
		}

		ProblemReport problemReport = merger.getProblemReport();
		return problemReport;
	}

	public ProblemReport mergeForDaily(String reportDomain, List<ProblemReport> reports, Set<String> domains) {
		ProblemReport report = merge(reportDomain, reports, true);
		Date date = report.getStartTime();
		Date end = new Date(TaskHelper.tomorrowZero(date).getTime() - 1000);

		report.setStartTime(TaskHelper.todayZero(date));
		report.setEndTime(end);
		report.getDomainNames().addAll(domains);
		return report;
	}

	public ProblemReport mergeForGraph(String reportDomain, List<ProblemReport> reports) {
		ProblemReport report = merge(reportDomain, reports, false);

		return report;
	}
}
