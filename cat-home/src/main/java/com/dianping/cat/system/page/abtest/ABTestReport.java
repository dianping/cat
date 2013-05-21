package com.dianping.cat.system.page.abtest;

import java.util.Date;
import java.util.Map;

import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestRun;

public class ABTestReport {

	private Abtest m_entity;
	
	private AbtestRun m_run;

	private AbtestStatus m_status;

	private Map<String, String> m_items;

	public ABTestReport(Abtest entity, AbtestRun run) {
		m_entity = entity;
		m_run = run;
	}

	public ABTestReport(Abtest entity, AbtestRun run, Date now) {
		m_entity = entity;
		m_run = run;
		setStatus(now);
		// TODO m_items setting
	}

	private void setStatus(Date now) {
		m_status = AbtestStatus.calculateStatus(m_run, now);
	}

	public Abtest getEntity() {
		return m_entity;
	}
	
	public AbtestRun getRun() {
   	return m_run;
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
