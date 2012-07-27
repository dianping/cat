/**
 * 
 */
package com.dianping.cat.report.task.problem;

import java.util.List;
import java.util.Set;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;
import com.dianping.cat.report.task.ReportMerger;

public class ProblemMerger implements ReportMerger<ProblemReport> {

	public ProblemReport mergeForDaily(String reportDomain, List<Report> reports, Set<String> domains) {
		ProblemReport report = merge(reportDomain, reports, true);
		report.getDomainNames().addAll(domains);
		return report;
	}

	@Override
	public ProblemReport mergeForGraph(String reportDomain, List<Report> reports) {
		ProblemReport report = merge(reportDomain, reports, false);
		return report;
	}

	private ProblemReport merge(String reportDomain, List<Report> reports, boolean isDaily) {
		ProblemReportMerger merger = null;
		if (isDaily) {
			merger = new HistoryProblemReportMerger(new ProblemReport(reportDomain));
		} else {
			merger = new ProblemReportMerger(new ProblemReport(reportDomain));
		}
		for (Report report : reports) {
			String xml = report.getContent();
			ProblemReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (Exception e) {
				Cat.logError(e);
			}
		}

		ProblemReport problemReport = merger.getProblemReport();
		return problemReport;
	}
}
