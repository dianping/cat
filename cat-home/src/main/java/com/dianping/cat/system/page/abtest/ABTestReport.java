package com.dianping.cat.system.page.abtest;

import java.util.Date;
import java.util.Map;

import com.dianping.cat.home.dal.abtest.Abtest;

public class ABTestReport {

	private Abtest m_entity;

	private ABTestEntityStatus m_status;

	private Map<String, String> m_items;

	private static final Date s_startTime = new Date();

	private static final Date s_endTime = new Date();

	public ABTestReport(Abtest entity){
		m_entity = entity;
	}
	
	public ABTestReport(Abtest entity, Date now) {
		m_entity = entity;
		if (m_entity.getStartDate() == null) {
			m_entity.setStartDate(s_startTime);
		}
		if (m_entity.getEndDate() == null) {
			m_entity.setEndDate(s_endTime);
		}

		setStatus(now);

		// TODO m_items
	}

	private void setStatus(Date now) {
		if (now.before(m_entity.getStartDate())) {
			m_status = ABTestEntityStatus.READY;
		} else if (now.before(m_entity.getEndDate())) {
			if (m_entity.isDisabled()) {
				m_status = ABTestEntityStatus.STOPPED;
			} else {
				m_status = ABTestEntityStatus.RUNNING;
			}
		} else {
			m_status = ABTestEntityStatus.DISABLED;
		}
	}

	public Abtest getEntity() {
		return m_entity;
	}
	
	public void setStatus(ABTestEntityStatus status) {
   	m_status = status;
   }

	public ABTestEntityStatus getStatus() {
		return m_status;
	}

	public Map<String, String> getItems() {
		return m_items;
	}

}
