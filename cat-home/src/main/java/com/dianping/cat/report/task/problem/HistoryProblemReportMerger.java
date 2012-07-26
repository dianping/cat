package com.dianping.cat.report.task.problem;

import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.report.page.model.problem.ProblemReportMerger;

public class HistoryProblemReportMerger extends ProblemReportMerger {
	
	public HistoryProblemReportMerger(ProblemReport problemReport) {
	   super(problemReport);
   }

	@Override
	public void visitThread(JavaThread thread) {
		// do nothing
	}

}
