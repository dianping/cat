package com.dianping.cat.report.page.state.task;

import java.util.Stack;

import com.dianping.cat.consumer.state.StateReportMerger;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;

public class HistoryStateReportMerger extends StateReportMerger {

	public HistoryStateReportMerger(StateReport stateReport) {
		super(stateReport);
	}

	protected void visitMachineChildren(Machine to, Machine from) {
		Stack<Object> objs = getObjects();

		for (ProcessDomain source : from.getProcessDomains().values()) {
			ProcessDomain target = to.findProcessDomain(source.getName());

			if (target == null) {
				target = new ProcessDomain(source.getName());
				to.addProcessDomain(target);
			}

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
	}

}
