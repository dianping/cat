package com.dianping.cat.system.page.abtest;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.dianping.cat.home.dal.abtest.Abtest;

public class ABTestReport {

	private static Date s_startTime;
	
	private static Date s_endTime;
	
	static{
		Calendar calendar = Calendar.getInstance();
		calendar.set(2000, 1, 1);
		s_startTime = calendar.getTime();
		calendar.set(2100, 1, 1);
		s_endTime = calendar.getTime();
	}
	
	private Abtest m_entity;

	private ABTestEntityStatus m_status;

	private Map<String, String> m_items;

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
