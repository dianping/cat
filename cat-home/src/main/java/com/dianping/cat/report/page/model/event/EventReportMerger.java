package com.dianping.cat.report.page.model.event;

import com.dianping.cat.consumer.event.StatisticsComputer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.transform.DefaultMerger;
import com.dianping.cat.helper.CatString;

public class EventReportMerger extends DefaultMerger {
	private boolean m_allIp = false;

	public EventReportMerger setAllIp(boolean allIp) {
		m_allIp = allIp;
		return this;
	}
	
	public EventReportMerger(EventReport eventReport) {
		super(eventReport);

		eventReport.accept(new StatisticsComputer());
	}

	
	@Override
   public void visitMachine(Machine machine) {
		if (m_allIp) {
			Machine newMachine = new Machine(CatString.ALL_IP);
			for (EventType type : machine.getTypes().values()) {
				newMachine.addType(type);
			}
			super.visitMachine(newMachine);
		} else {
			super.visitMachine(machine);
		}
   }

	@Override
	public void visitEventReport(EventReport eventReport) {
		super.visitEventReport(eventReport);
		getEventReport().getDomainNames().addAll(eventReport.getDomainNames());
		getEventReport().getIps().addAll(eventReport.getIps());
	}

	@Override
	protected void mergeName(EventName old, EventName other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

	@Override
	protected void mergeRange(Range old, Range range) {
		old.setCount(old.getCount() + range.getCount());
		old.setFails(old.getFails() + range.getFails());
	}

	public EventReport mergesFrom(EventReport report) {
		report.accept(this);

		return getEventReport();
	}

	@Override
	protected void mergeEventReport(EventReport old, EventReport eventReport) {
		super.mergeEventReport(old, eventReport);

	}

	@Override
	protected void mergeType(EventType old, EventType other) {
		old.setTotalCount(old.getTotalCount() + other.getTotalCount());
		old.setFailCount(old.getFailCount() + other.getFailCount());

		if (old.getTotalCount() > 0) {
			old.setFailPercent(old.getFailCount() * 100.0 / old.getTotalCount());
		}

		if (old.getSuccessMessageUrl() == null) {
			old.setSuccessMessageUrl(other.getSuccessMessageUrl());
		}

		if (old.getFailMessageUrl() == null) {
			old.setFailMessageUrl(other.getFailMessageUrl());
		}
	}

}
