package com.dianping.cat.system.page.abtest;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("status")
	private String m_status;

	@FieldMeta("pageNum")
	private int m_pageNum;

	@FieldMeta("enable")
	private boolean m_enableAbtest;

	@FieldMeta("ids")
	private String m_ids;

	public void setAction(String action) {
		if (action.equalsIgnoreCase(Action.REPORT.getName())) {
			m_action = Action.getByName(action, Action.REPORT);
		} else if (action.equalsIgnoreCase(Action.LIST.getName())) {
			m_action = Action.getByName(action, Action.LIST);
		} else {
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
		AbtestStatus abstatus = AbtestStatus.getByName(status, null);

		if (abstatus != null) {
			m_status = abstatus.name().toLowerCase();
		} else {
			m_status = "all";
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

	public boolean isEnableAbtest() {
		return m_enableAbtest;
	}

	public void setEnableAbtest(boolean enableAbtest) {
		m_enableAbtest = enableAbtest;
	}

	public String getIds() {
		return m_ids;
	}

	public void setIds(String ids) {
		m_ids = ids;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
		if (m_status == null) {
			m_status = "all";
		}
	}
}
