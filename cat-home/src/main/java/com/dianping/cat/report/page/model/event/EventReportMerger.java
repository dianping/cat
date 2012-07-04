package com.dianping.cat.report.page.model.event;

import java.util.Map;

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

	private boolean m_allName = false;

	private String m_ip;

	private String m_type;

	public EventReportMerger(EventReport eventReport) {
		super(eventReport);

		eventReport.accept(new StatisticsComputer());
	}

	@Override
	protected void mergeEventReport(EventReport old, EventReport eventReport) {
		super.mergeEventReport(old, eventReport);

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

	public EventName mergesFor(String typeName, String ip) {
		EventName name = new EventName("ALL");
		EventReport report = getEventReport();
		EventType type = report.getMachines().get(ip).findType(typeName);

		if (type != null) {
			for (EventName n : type.getNames().values()) {
				mergeName(name, n);
				visitNameChildren(name, n);
			}
		}

		return name;
	}

	public Machine mergesForAllMachine(EventReport report) {
		Machine machine = new Machine(CatString.ALL_IP);
		for (Machine temp : report.getMachines().values()) {
			if (!machine.getIp().equals(CatString.ALL_IP)) {
				mergeMachine(machine, temp);
			}
			visitMachineChildren(machine, temp);
		}
		return machine;
	}

	public EventName mergesForAllName(EventReport report) {
		EventName name = new EventName("ALL");
		EventType type = report.getMachines().get(m_ip).findType(m_type);

		if (type != null) {
			for (EventName n : type.getNames().values()) {
				mergeName(name, n);
				visitNameChildren(name, n);
			}
		}

		return name;
	}

	public EventReport mergesFrom(EventReport report) {
		report.accept(this);

		return getEventReport();
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

	public void setAllName(boolean allName) {
		m_allName = allName;
	}

	public void setIp(String ip) {
		m_ip = ip;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void visitEventReport(EventReport eventReport) {
		if (m_allIp) {
			Map<String, Machine> machines = eventReport.getMachines();
			Machine allMachines = mergesForAllMachine(eventReport);
			machines.clear();
			eventReport.addMachine(allMachines);
		}
		if (m_allName) {
			Machine machine = eventReport.getMachines().get(m_ip);
			if (machine != null) {
				EventName mergesForAllName = mergesForAllName(eventReport);
				EventType type = machine.getTypes().get(m_type);
				type.getNames().clear();
				type.addName(mergesForAllName);
			}
		}
		super.visitEventReport(eventReport);
		getEventReport().getDomainNames().addAll(eventReport.getDomainNames());
		getEventReport().getIps().addAll(eventReport.getIps());
	}
	
	
}
