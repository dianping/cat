package com.dianping.cat.system.page.abtest;

import java.util.Date;
import java.util.List;

import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.abtest.spi.ABTestEntity;
import com.dianping.cat.system.SystemPage;

public class Model extends ViewModel<SystemPage, Action, Context> {
	private String m_domain;

	private Date m_date;
	
	private ABTestEntity m_entity;
	
	private List<ABTestReport> m_reports;
	
	private int m_totalPages;

	private int m_runningCount;
	
	private int m_readyCount;
	
	private int m_stoppedCount;
	
	private int m_disabledCount;
	
	public Model(Context ctx) {
		super(ctx);
	}

	public Date getDate() {
		return m_date;
	}

	public void setDate(Date date) {
		m_date = date;
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	public String getDomain() {
		return m_domain;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public ABTestEntity getEntity() {
   	return m_entity;
   }

	public void setEntity(ABTestEntity entity) {
   	m_entity = entity;
   }

	public List<ABTestReport> getReports() {
   	return m_reports;
   }

	public void setReports(List<ABTestReport> reports) {
   	m_reports = reports;
   }
	
	public int getTotalPages() {
   	return m_totalPages;
   }

	public void setTotalPages(int totalPages) {
   	m_totalPages = totalPages;
   }

	public int getRunningCount() {
   	return m_runningCount;
   }

	public void setRunningCount(int runningCount) {
   	m_runningCount = runningCount;
   }

	public int getReadyCount() {
   	return m_readyCount;
   }

	public void setReadyCount(int readyCount) {
   	m_readyCount = readyCount;
   }

	public int getStoppedCount() {
   	return m_stoppedCount;
   }

	public void setStoppedCount(int stoppedCount) {
   	m_stoppedCount = stoppedCount;
   }

	public int getDisabledCount() {
   	return m_disabledCount;
   }

	public void setDisabledCount(int disabledCount) {
   	m_disabledCount = disabledCount;
   }
	
}
