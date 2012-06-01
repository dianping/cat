package com.dianping.cat.report.page.trend;

import java.util.Date;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;
	
	@FieldMeta("date")
	private Date m_date;
	
	@FieldMeta("graphType")
	private String m_graphType;
	
	@FieldMeta("domain")
	private String m_domain;
	
	@FieldMeta("queryIP")
	private String m_queryIP;
	
	@FieldMeta("queryType")
	private String m_queryType;
	
	@FieldMeta("queryName")
	private String m_queryName;
	
	@FieldMeta("dateType")
	private String m_dateType;
	
	@FieldMeta("startDate")
	private String m_startDate;

	@FieldMeta("endDate")
	private String m_endDate;
	
	@FieldMeta("selfQueryOption")
	private String m_selfQueryOption;
	
	

	public String getDateType() {
   	return m_dateType;
   }

	public void setDateType(String dateType) {
   	m_dateType = dateType;
   }

	public String getQueryDate() {
   	return m_startDate;
   }

	public void setQueryDate(String queryDate) {
   	m_startDate = queryDate;
   }

	public String getSelfQueryOption() {
   	return m_selfQueryOption;
   }

	public void setSelfQueryOption(String selfQueryOption) {
   	m_selfQueryOption = selfQueryOption;
   }

	public void setQueryIP(String queryIP) {
   	m_queryIP = queryIP;
   }

	public String getQueryIP() {
   	return m_queryIP;
   }

	public String getQueryType() {
   	return m_queryType;
   }

	public void setQueryType(String queryType) {
   	m_queryType = queryType;
   }

	public String getQueryName() {
   	return m_queryName;
   }

	public void setQueryName(String queryName) {
   	m_queryName = queryName;
   }

	@FieldMeta("op")
	private Action m_action;
	
	
	public Date getDate() {
   	return m_date;
   }

	public void setDate(Date date) {
   	m_date = date;
   }

	public String getGraphType() {
   	return m_graphType;
   }

	public void setGraphType(String graphType) {
   	m_graphType = graphType;
   }

	public void setPage(ReportPage page) {
   	m_page = page;
   }

	public String getDomain() {
   	return m_domain;
   }

	public void setDomain(String domain) {
   	m_domain = domain;
   }

	public void setAction(Action action) {
		m_action = action;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TREND);
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

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
