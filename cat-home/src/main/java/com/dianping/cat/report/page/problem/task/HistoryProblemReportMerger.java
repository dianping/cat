package com.dianping.cat.report.page.problem.task;

import java.util.Stack;

import com.dianping.cat.consumer.problem.ProblemReportMerger;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;

public class HistoryProblemReportMerger extends ProblemReportMerger {

	public HistoryProblemReportMerger(ProblemReport problemReport) {
		super(problemReport);
	}

	@Override
	protected void visitMachineChildren(Machine to, Machine from) {
		Stack<Object> objs = getObjects();

		for (Entity source : from.getEntities().values()) {
			Entity target = findOrCreateEntity(to, source);

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
	}

	@Override
	protected void visitEntityChildren(Entity to, Entity from) {
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
