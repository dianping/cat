package com.dianping.cat.report.page.model.event;

import com.dianping.cat.consumer.event.StatisticsComputer;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.DefaultMerger;
import com.dianping.cat.helper.CatString;

public class EventReportMerger extends DefaultMerger {
	private boolean m_allIp = false;

	private Machine m_allMachines;

	private boolean m_allName = false;

	private EventName m_allNames;

	private String m_ip;

	private String m_type;

	public EventReportMerger(EventReport eventReport) {
		super(eventReport);

		eventReport.accept(new StatisticsComputer());
	}

	@Override
	protected void mergeMachine(Machine old, Machine machine) {
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

	public Machine mergesForAllMachine(EventReport report) {
		Machine machine = new Machine(CatString.ALL_IP);

		for (Machine m : report.getMachines().values()) {
			if (!m.getIp().equals(CatString.ALL_IP)) {
				visitMachineChildren(machine, m);
			}
		}

		return machine;
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

	public EventReportMerger setAllIp(boolean allIp) {
		m_allIp = allIp;
		return this;
	}

	public EventReportMerger setAllName(boolean allName) {
		m_allName = allName;
		return this;
	}

	public EventReportMerger setIp(String ip) {
		m_ip = ip;
		return this;
	}

	public EventReportMerger setType(String type) {
		m_type = type;
		return this;
	}

	@Override
	public void visitMachine(Machine machine) {
		if (m_allIp) {
			visitMachineChildren(m_allMachines, machine);
		} else {
			super.visitMachine(machine);
		}
	}

	@Override
	public void visitName(EventName name) {
		if (m_allName) {
			visitNameChildren(m_allNames, name);
		} else {
			super.visitName(name);
		}
	}

	@Override
	public void visitEventReport(EventReport eventReport) {
		EventReport report = getEventReport();

		if (m_allIp) {
			m_allMachines = report.findOrCreateMachine(CatString.ALL_IP);
		}

		if (m_allName) {
			m_allNames = report.findOrCreateMachine(m_ip).findOrCreateType(m_type).findOrCreateName("ALL");
		}

		super.visitEventReport(eventReport);
		report.getDomainNames().addAll(eventReport.getDomainNames());
		report.getIps().addAll(eventReport.getIps());
	}

	@Override
	public void visitType(EventType type) {
		if (!m_allName || m_allName && m_type.equals(type.getId())) {
			super.visitType(type);
		}
	}
}
