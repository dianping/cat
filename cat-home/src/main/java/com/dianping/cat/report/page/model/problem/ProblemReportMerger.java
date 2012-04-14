package com.dianping.cat.report.page.model.problem;

import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.transform.DefaultMerger;

public class ProblemReportMerger extends DefaultMerger {
	public ProblemReportMerger(ProblemReport problemReport) {
	   super(problemReport);
   }

	
	@Override
   public void visitProblemReport(ProblemReport problemReport) {
	   super.visitProblemReport(problemReport);
	   
	   getProblemReport().getIps().addAll(problemReport.getIps());
	   getProblemReport().getDomainNames().addAll(problemReport.getDomainNames());
   }
}
