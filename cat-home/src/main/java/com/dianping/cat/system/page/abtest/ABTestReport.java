package com.dianping.cat.system.page.abtest;

import java.util.Date;
import java.util.Map;

import com.dianping.cat.home.dal.abtest.Abtest;

public class ABTestReport {

	private Abtest m_entity;

	private AbtestStatus m_status;

	private Map<String, String> m_items;

	public ABTestReport(Abtest entity) {
		m_entity = entity;
	}

	public ABTestReport(Abtest entity, Date now) {
		m_entity = entity;
		setStatus(now);
		// TODO m_items setting
	}

	private void setStatus(Date now) {
		m_status = AbtestStatus.calculateStatus(m_entity, now);
	}

	public Abtest getEntity() {
		return m_entity;
	}

	public void setStatus(AbtestStatus status) {
		m_status = status;
	}

	public AbtestStatus getStatus() {
		return m_status;
	}

	public Map<String, String> getItems() {
		return m_items;
	}

}
