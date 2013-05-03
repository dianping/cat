package com.dianping.cat.system.page.abtest;

import com.dianping.cat.system.SystemPage;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;
	
	@FieldMeta("status")
	private String m_status;
	
	@FieldMeta("pageNum")
	private int m_pageNum;
	
	public void setAction(String action) {
		if(action.equalsIgnoreCase(Action.REPORT.getName())){
			m_action = Action.getByName(action, Action.REPORT);
		}else if(action.equalsIgnoreCase(Action.LIST.getName())){
			m_action = Action.getByName(action, Action.LIST);
		}else{
			m_action = Action.getByName(action, Action.VIEW);
		}
	}

	@Override
	public Action getAction() {
		return m_action;
	}
	
	public String getStatus() {
   	return m_status;
   }

	public void setStatus(String status) {
		if(StringUtils.isBlank(status)){
			m_status = ABTestEntityStatus.DEFALUT.name();
		}else{
			m_status = status;
		}
   }

	public int getPageNum() {
   	return m_pageNum;
   }

	public void setPageNum(int pageNum) {
   	m_pageNum = pageNum;
   }

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public String getReportType() {
		return "";
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.ABTEST);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
