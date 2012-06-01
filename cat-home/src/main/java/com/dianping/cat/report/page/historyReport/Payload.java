package com.dianping.cat.report.page.historyReport;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("startDate")
	private String m_startDate;

	@FieldMeta("endDate")
	private String m_endDate;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("ip")
	private String m_ip;
	
	@FieldMeta("sort")
	private String m_sortBy;
	
	@FieldMeta("type")
	private String type;

	@FieldMeta("threshold")
	private int m_longTime;
	
	public void setAction(String action) {
		m_action = Action.getByName(action, Action.TRANSACTION);
	}

	@Override
	public Action getAction() {
		return m_action != null ? m_action : Action.TRANSACTION;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.HISTORYREPORT);
	}

	public String getStartDate() {
		return m_startDate;
	}

	public void setStartDate(String startDate) {
		m_startDate = startDate;
	}

	public String getEndDate() {
		return m_endDate;
	}

	public void setEndDate(String endDate) {
		m_endDate = endDate;
	}

	public String getType() {
   	return type;
   }

	public void setType(String type) {
   	this.type = type;
   }

	public String getDomain() {
   	return m_domain;
   }

	public void setDomain(String domain) {
   	m_domain = domain;
   }

	public String getIp() {
   	return m_ip;
   }

	public void setIp(String ip) {
		m_ip = ip;
   }

	public String getSortBy() {
   	return m_sortBy;
   }

	public void setSortBy(String sortBy) {
   	m_sortBy = sortBy;
   }

	public int getLongTime() {
   	return m_longTime;
   }

	public void setLongTime(int longTime) {
   	m_longTime = longTime;
   }

	@Override
	public void validate(ActionContext<?> ctx) {
		m_action = m_action != null ? m_action : Action.TRANSACTION;
	}
}
