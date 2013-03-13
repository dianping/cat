package com.dianping.cat.system.page.login;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.system.SystemPage;

public class Payload implements ActionPayload<SystemPage, Action> {
	private SystemPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("rtnUrl")
	private String m_rtnUrl;

	@FieldMeta("account")
	private String m_account;

	@FieldMeta("password")
	private String m_password;

	@FieldMeta("login")
	private boolean m_submit;

	public String getAccount() {
		return m_account;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public SystemPage getPage() {
		return m_page;
	}

	public String getPassword() {
		return m_password;
	}

	public String getRtnUrl() {
		return m_rtnUrl;
	}

	public boolean isSubmit() {
		return m_submit;
	}

	public void setAccount(String account) {
		m_account = account;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.LOGIN);
	}

	@Override
	public void setPage(String page) {
		m_page = SystemPage.getByName(page, SystemPage.LOGIN);
	}

	public void setPassword(String password) {
		m_password = password;
	}

	public void setRtnUrl(String rtnUrl) {
		m_rtnUrl = rtnUrl;
	}

	public void setSubmit(boolean submit) {
		m_submit = submit;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.LOGIN;
		}
	}
}
