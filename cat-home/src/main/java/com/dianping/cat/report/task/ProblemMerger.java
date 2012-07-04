/**
 * 
 */
package com.dianping.cat.report.task;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.xml.sax.SAXException;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultSaxParser;
import com.dianping.cat.hadoop.dal.Report;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;

/**
 * @author sean.wang
 * @since Jun 20, 2012
 */
public class ProblemMerger implements ReportMerger<ProblemReport> {

	private ProblemReport mergeForDaily(String reportDomain, List<Report> reports) {
		//Theadsinfo is no use
		ProblemReportMerger merger = new HistoryProblemReportMerger(new ProblemReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			ProblemReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ProblemReport problemReport = merger.getProblemReport();
		return problemReport;
	}

	@Override
	public ProblemReport merge(String reportDomain, List<Report> reports) {
		ProblemReportMerger merger = new ProblemReportMerger(new ProblemReport(reportDomain));

		for (Report report : reports) {
			String xml = report.getContent();
			ProblemReport model;
			try {
				model = DefaultSaxParser.parse(xml);
				model.accept(merger);
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		ProblemReport problemReport = merger.getProblemReport();
		return problemReport;
	}
	
	@Override
	public String mergeAll(String reportDomain, List<Report> reports, Set<String> domains) {
		ProblemReport report = mergeForDaily(reportDomain, reports);
		report.getDomainNames().addAll(domains);
		return report.toString();
	}
}
