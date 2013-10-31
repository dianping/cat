package com.dianping.cat.report.task.state;

import java.util.Stack;

import com.dianping.cat.consumer.state.StateReportMerger;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;

public class HistoryStateReportMerger extends StateReportMerger {

	public HistoryStateReportMerger(StateReport stateReport) {
		super(stateReport);
	}

	protected void visitMachineChildren(Machine old, Machine machine) {
		Stack<Object> objs = getObjects();

		for (ProcessDomain source : machine.getProcessDomains().values()) {
			ProcessDomain target = old.findProcessDomain(source.getName());

			if (target == null) {
				target = new ProcessDomain(source.getName());
				old.addProcessDomain(target);
			}

			objs.push(target);
			source.accept(this);
			objs.pop();
		}
		
		super.visitMachineChildren(machine, old);
	}

}
