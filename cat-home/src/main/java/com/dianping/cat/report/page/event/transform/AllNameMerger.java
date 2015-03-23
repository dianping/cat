package com.dianping.cat.report.page.event.transform;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.event.EventReportMerger;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public class AllNameMerger extends BaseVisitor {

	public EventReport m_report;

	public String m_currentIp;

	public String m_currentType;

	public String m_currentName;

	public Integer m_currentRange;

	public EventReportMerger m_merger = new EventReportMerger(new EventReport());

	public EventReport getReport() {
		return m_report;
	}

	@Override
	public void visitEventReport(EventReport eventReport) {
		m_report = new EventReport(eventReport.getDomain());
		m_report.setStartTime(eventReport.getStartTime());
		m_report.setEndTime(eventReport.getEndTime());
		m_report.getDomainNames().addAll(eventReport.getDomainNames());
		m_report.getIps().addAll(eventReport.getIps());

		super.visitEventReport(eventReport);
	}

	@Override
	public void visitMachine(Machine machine) {
		m_currentIp = machine.getIp();
		m_report.findOrCreateMachine(m_currentIp);
		super.visitMachine(machine);
	}

	@Override
	public void visitName(EventName name) {
		m_currentName = name.getId();
		EventName temp = m_report.findOrCreateMachine(m_currentIp).findOrCreateType(m_currentType)
		      .findOrCreateName(m_currentName);

		m_merger.mergeName(temp, name);

		EventName all = m_report.findOrCreateMachine(m_currentIp).findOrCreateType(m_currentType)
		      .findOrCreateName(Constants.ALL);
		m_merger.mergeName(all, name);

		m_merger.mergeName(temp, name);
		super.visitName(name);
	}

	@Override
	public void visitRange(Range range) {
		m_currentRange = range.getValue();
		Range temp = m_report.findOrCreateMachine(m_currentIp).findOrCreateType(m_currentType)
		      .findOrCreateName(m_currentName).findOrCreateRange(m_currentRange);

		m_merger.mergeRange(temp, range);

		Range all = m_report.findOrCreateMachine(m_currentIp).findOrCreateType(m_currentType)
		      .findOrCreateName(Constants.ALL).findOrCreateRange(m_currentRange);

		m_merger.mergeRange(all, range);
		super.visitRange(range);
	}

	@Override
	public void visitType(EventType type) {
		m_currentType = type.getId();
		EventType temp = m_report.findOrCreateMachine(m_currentIp).findOrCreateType(m_currentType);

		m_merger.mergeType(temp, type);
		super.visitType(type);
	}

}
