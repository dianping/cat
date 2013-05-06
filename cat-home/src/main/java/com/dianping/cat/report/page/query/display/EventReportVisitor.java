package com.dianping.cat.report.page.query.display;

import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.transform.BaseVisitor;

public  class EventReportVisitor extends BaseVisitor {
	private String m_type;

	private String m_name;

	private String m_currentType;

	private String m_currentName;

	public EventQueryItem m_item = new EventQueryItem();

	public EventReportVisitor(String type, String name) {
		m_type = type;
		m_name = name;
		m_item.setType(type);
		m_item.setName(name);
	}

	public EventQueryItem getItem() {
		return m_item;
	}

	public void setItem(EventQueryItem item) {
		m_item = item;
	}

	@Override
	public void visitEventReport(EventReport eventReport) {
		super.visitEventReport(eventReport);
		m_item.setDate(eventReport.getStartTime());
	}

	@Override
	public void visitName(EventName name) {
		m_currentName = name.getId();
		if (m_type.equalsIgnoreCase(m_currentType) && m_name.equalsIgnoreCase(m_currentName)) {
			m_item.setTotalCount(name.getTotalCount());
			m_item.setFailCount(name.getFailCount());
			m_item.setFailPercent(name.getFailPercent());
		}
	}

	@Override
	public void visitType(EventType type) {
		m_currentType = type.getId();
		if (m_name == null || m_name.trim().length() == 0) {
			if (m_type.equalsIgnoreCase(m_currentType)) {
				m_item.setTotalCount(type.getTotalCount());
				m_item.setFailCount(type.getFailCount());
				m_item.setFailPercent(type.getFailPercent());
			}
		} else {
			super.visitType(type);
		}
	}

}
