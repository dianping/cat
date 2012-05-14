package com.dianping.cat.report.page.transaction;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("name")
	private String m_name;
	
	@FieldMeta("sort")
	private String m_sortBy;
	
	@FieldMeta("queryname")
	private String m_queryName;

	public Payload() {
		super(ReportPage.TRANSACTION);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getName() {
		return m_name;
	}

	public String getType() {
		return m_type;
	}
	
	public String getQueryName(){
		return m_queryName;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setType(String type) {
		m_type = type;
	}
	
	public void setQueryName(String queryName){
		this.m_queryName=queryName;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}

	public String getSortBy() {
   	return m_sortBy;
   }

	public void setSortBy(String sortBy) {
   	m_sortBy = sortBy;
   }
	
}
