package com.dianping.cat.consumer.event;

import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Machine;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public class EventReportTypeAggregator extends BaseVisitor {

	private EventReport m_report;

	public String m_currentDomain;

	private String m_currentType;

	private AllReportConfigManager m_configManager;

	public EventReportTypeAggregator(EventReport report, AllReportConfigManager configManager) {
		m_report = report;
		m_configManager = configManager;
	}

	private void mergeName(EventName old, EventName other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();

		old.setTotalCount(totalCountSum);
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

	private void mergeType(EventType old, EventType other) {
		long totalCountSum = old.getTotalCount() + other.getTotalCount();

		old.setTotalCount(totalCountSum);
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

	private boolean validateName(String type, String name) {
		return m_configManager.validate(EventAnalyzer.ID, type, name);
	}

	private boolean validateType(String type) {
		return m_configManager.validate(EventAnalyzer.ID, type);
	}

	@Override
	public void visitName(EventName name) {
		if (validateName(m_currentType, name.getId())) {
			Machine machine = m_report.findOrCreateMachine(m_currentDomain);
			EventType curentType = machine.findOrCreateType(m_currentType);
			EventName currentName = curentType.findOrCreateName(name.getId());

			mergeName(currentName, name);
		}
	}

	@Override
	public void visitEventReport(EventReport eventReport) {
		m_currentDomain = eventReport.getDomain();
		m_report.setStartTime(eventReport.getStartTime());
		m_report.setEndTime(eventReport.getEndTime());
		super.visitEventReport(eventReport);
	}

	@Override
	public void visitType(EventType type) {
		String typeName = type.getId();

		if (validateType(typeName)) {
			Machine machine = m_report.findOrCreateMachine(m_currentDomain);
			EventType result = machine.findOrCreateType(typeName);

			m_currentType = typeName;
			mergeType(result, type);

			super.visitType(type);
		}
	}
}