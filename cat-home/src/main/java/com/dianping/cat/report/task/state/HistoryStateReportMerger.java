package com.dianping.cat.report.task.state;

import static com.dianping.cat.consumer.state.model.Constants.ENTITY_PROCESSDOMAINS;

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
		if (old != null) {
			Stack<Object> objs = getObjects();
			Stack<String> tags = getTags();
			objs.push(old);

			for (ProcessDomain processDomain : machine.getProcessDomains().values()) {
				tags.push(ENTITY_PROCESSDOMAINS);
				visitProcessDomain(processDomain);
				tags.pop();
			}
			objs.pop();
		}
	}

}
