package com.dianping.cat.report.task.problem;

import java.util.Stack;

import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;

public class HistoryProblemReportMerger extends ProblemReportMerger {

	public HistoryProblemReportMerger(ProblemReport problemReport) {
		super(problemReport);
	}

	protected void visitEntryChildren(Entry to, Entry from) {
		Stack<Object> objs = getObjects();
		for (Duration source : from.getDurations().values()) {
			Duration target = to.findDuration(source.getValue());

			if (target == null) {
				target = new Duration(source.getValue());
				to.addDuration(target);
			}

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
	}
}
