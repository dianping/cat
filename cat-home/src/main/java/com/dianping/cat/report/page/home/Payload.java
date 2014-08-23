package com.dianping.cat.report.page.home;

import org.unidal.lookup.util.StringUtils;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("docName")
	private String m_docName;

	public Payload() {
		super(ReportPage.HOME);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getDocName() {
		if (StringUtils.isEmpty(m_docName)) {
			return "dianping";
		} else {
			return m_docName;
		}
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setDocName(String docName) {
		m_docName = docName;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
