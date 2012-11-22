package com.dianping.cat.report.page.model.state;

import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultMerger;

public class StateReportMerger extends DefaultMerger {

	public StateReportMerger(StateReport stateReport) {
		super(stateReport);
	}

	@Override
   protected void mergeMachine(Machine old, Machine machine) {
	   super.mergeMachine(old, machine);
   }

	@Override
   protected void mergeMessage(Message old, Message message) {
		old.setTotal(message.getTotal());
		old.setTotalLoss(message.getTotalLoss());
		old.setTime(message.getTime());
		old.setSize(message.getSize());
		old.setDumpLoss(message.getDumpLoss());
		old.setDump(message.getDump());
		old.setDelayCount(message.getDelayCount());
		old.setDelaySum(message.getDelaySum());
   }

	@Override
   protected void mergeProcessDomain(ProcessDomain old, ProcessDomain processDomain) {
	   super.mergeProcessDomain(old, processDomain);
   }

	@Override
   public void visitStateReport(StateReport stateReport) {
	   super.visitStateReport(stateReport);
	   
	   StateReport report = getStateReport();
	   report.setDomain(stateReport.getDomain());
	   report.setStartTime(stateReport.getStartTime());
	   report.setEndTime(stateReport.getEndTime());
   }
	
}
