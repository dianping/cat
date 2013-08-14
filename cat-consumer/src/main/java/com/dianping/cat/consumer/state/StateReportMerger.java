package com.dianping.cat.consumer.state;

import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.Message;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.DefaultMerger;

public class StateReportMerger extends DefaultMerger {

	public StateReportMerger(StateReport stateReport) {
		super(stateReport);
	}

//	private long getTotal(Machine machine, String key) {
//		return machine.getTotals().get(key).getCount();
//	}
//
//	private long getTotalLoss(Machine machine, String key) {
//		return machine.getTotalLosses().get(key).getCount();
//	}
//
//	private double getSize(Machine machine, String key) {
//		return machine.getSizes().get(key).getCount();
//	}

	@Override
	protected void mergeMachine(Machine old, Machine machine) {
		double oldCount = 0;
		double newCount = 0;
		if (old.getAvgTps() > 0) {
			oldCount = old.getTotal() / old.getAvgTps();
		}
		if (machine.getAvgTps() > 0) {
			newCount = machine.getTotal() / machine.getAvgTps();
		}
		double totalCount = oldCount + newCount;
		if (totalCount > 0) {
			old.setAvgTps((old.getTotal() + machine.getTotal()) / totalCount);
		}
//		for (String key : old.getTotals().keySet()) {
//			old.findOrCreateTotal(key).setCount(getTotal(old, key) + getTotal(machine, key));
//		}
//
//		for (String key : old.getTotalLosses().keySet()) {
//			old.findOrCreateTotalLoss(key).setCount(getTotalLoss(old, key) + getTotalLoss(machine, key));
//		}
//
//		for (String key : old.getTotals().keySet()) {
//			old.findOrCreateSize(key).setCount(getSize(old, key) + getSize(machine, key));
//		}
		old.setTotal(old.getTotal() + machine.getTotal());
		old.setTotalLoss(old.getTotalLoss() + machine.getTotalLoss());
		old.setSize(old.getSize() + machine.getSize());
		
		old.setDump(old.getDump() + machine.getDump());
		old.setDumpLoss(old.getDumpLoss() + machine.getDumpLoss());
		old.setDelaySum(old.getDelaySum() + machine.getDelaySum());
		old.setDelayCount(old.getDelayCount() + machine.getDelayCount());

		old.setBlockTotal(old.getBlockTotal() + machine.getBlockTotal());
		old.setBlockLoss(old.getBlockLoss() + machine.getBlockLoss());
		old.setBlockTime(old.getBlockTime() + machine.getBlockTime());
		old.setPigeonTimeError(old.getPigeonTimeError() + machine.getPigeonTimeError());
		old.setNetworkTimeError(old.getNetworkTimeError() + machine.getNetworkTimeError());

		if (machine.getMaxTps() > old.getMaxTps()) {
			old.setMaxTps(machine.getMaxTps());
		}

		long count = old.getDelayCount();
		double sum = old.getDelaySum();
		if (count > 0) {
			old.setDelayAvg(sum / count);
		}
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
		old.setBlockTotal(message.getBlockTotal());
		old.setBlockLoss(message.getBlockLoss());
		old.setBlockTime(message.getBlockTime());
		old.setPigeonTimeError(message.getPigeonTimeError());
		old.setNetworkTimeError(message.getNetworkTimeError());
	}

	@Override
	protected void mergeProcessDomain(ProcessDomain old, ProcessDomain processDomain) {
		super.mergeProcessDomain(old, processDomain);
		old.getIps().addAll(processDomain.getIps());
		old.setTotal(old.getTotal() + processDomain.getTotal());
		old.setTotalLoss(old.getTotalLoss() + processDomain.getTotalLoss());
		old.setSize(old.getSize() + processDomain.getSize());
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
