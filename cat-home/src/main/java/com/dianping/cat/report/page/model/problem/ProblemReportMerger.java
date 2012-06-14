package com.dianping.cat.report.page.model.problem;

import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.DefaultMerger;

public class ProblemReportMerger extends DefaultMerger {
	public ProblemReportMerger(ProblemReport problemReport) {
	   super(problemReport);
   }

	
	@Override
   protected void mergeEntry(Entry old, Entry entry) {
	   // TODO Auto-generated method stub
	   super.mergeEntry(old, entry);
   }

	

	@Override
   protected void mergeSegment(Segment old, Segment segment) {
	   super.mergeSegment(old, segment);
   }


	@Override
   public void visitProblemReport(ProblemReport problemReport) {
	   super.visitProblemReport(problemReport);
	   
	   getProblemReport().getIps().addAll(problemReport.getIps());
	   getProblemReport().getDomainNames().addAll(problemReport.getDomainNames());
   }
}
