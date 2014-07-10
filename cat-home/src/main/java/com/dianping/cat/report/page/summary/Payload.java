package com.dianping.cat.report.page.summary;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("time")
	private String m_time;

	@FieldMeta("emails")
	private String m_emails;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	public Action getAction() {
		if (m_action == null) {
			return Action.VIEW;
		} else {
			return m_action;
		}
	}

	public String getDomain() {
		if (m_domain == null || "".equals(m_domain)) {
			return null;
		} else {
			return m_domain;
		}
	}

	public String getEmails() {
		if (m_emails == null || "".equals(m_emails)) {
			return null;
		} else {
			return m_emails;
		}
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public Date getTime() {
		try {
			return m_sdf.parse(m_time);
		} catch (Exception ex) {
			return new Date();
		}
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setEmails(String emails) {
		m_emails = emails;
	}

	public void setPage(ReportPage page) {
		m_page = page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.SUMMARY);
	}

	public void setTime(String time) {
		m_time = time;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
