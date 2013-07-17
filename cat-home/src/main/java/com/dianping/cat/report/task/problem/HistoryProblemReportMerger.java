package com.dianping.cat.report.task.problem;

import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;

public class HistoryProblemReportMerger extends ProblemReportMerger {

	public HistoryProblemReportMerger(ProblemReport problemReport) {
		super(problemReport);
	}

	@Override
	public void visitThread(JavaThread thread) {
		// do nothing
	}
}
