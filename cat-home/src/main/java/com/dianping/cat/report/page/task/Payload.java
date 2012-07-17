package com.dianping.cat.report.page.task;

import java.util.Date;

import com.dianping.cat.report.ReportPage;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	
	
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;
	
	@FieldMeta("pagesize")
	private int pagesize;
	
	@FieldMeta("currentPage")
	private int currentPage;
	
	@FieldMeta("start")
	private Date start;
	
	@FieldMeta("end")
	private Date end;
	
	@FieldMeta("status")
	private String status;
	
	@FieldMeta("type")
	private String type;
	
	@FieldMeta("name")
	private String name;
	
	@FieldMeta("domain")
	private String domain;
	
	

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
		m_page = ReportPage.getByName(page, ReportPage.TASK);
	}

	public int getPagesize() {
   	return pagesize;
   }

	public void setPagesize(int pagesize) {
   	this.pagesize = pagesize;
   }

	public int getCurrentPage() {
   	return currentPage;
   }

	public void setCurrentPage(int currentPage) {
   	this.currentPage = currentPage;
   }

	public Date getStart() {
   	return start;
   }

	public void setStart(Date start) {
   	this.start = start;
   }

	public Date getEnd() {
   	return end;
   }

	public void setEnd(Date end) {
   	this.end = end;
   }

	public String getStatus() {
   	return status;
   }

	public void setStatus(String status) {
   	this.status = status;
   }

	public String getType() {
   	return type;
   }

	public void setType(String type) {
   	this.type = type;
   }

	public String getName() {
   	return name;
   }

	public void setName(String name) {
   	this.name = name;
   }

	public String getDomain() {
   	return domain;
   }

	public void setDomain(String domain) {
   	this.domain = domain;
   }

	public void setPage(ReportPage page) {
   	m_page = page;
   }

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
