package com.dianping.cat.report.page.transaction;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.model.spi.ModelPeriod;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("domain")
	private String m_domain;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("hours")
	private int m_hours;

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getDomain() {
		return m_domain;
	}

	public int getHours() {
		return m_hours;
	}

	public String getName() {
		return m_name;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public ModelPeriod getPeriod() {
		if (m_hours == 0) {
			return ModelPeriod.CURRENT;
		} else if (m_hours == -1) {
			return ModelPeriod.LAST;
		} else if (m_hours < 0) {
			return ModelPeriod.HISTORICAL;
		} else {
			return ModelPeriod.FUTURE;
		}
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setHours(String hours) {
		if (hours != null) {
			try {
				m_hours = Integer.parseInt(hours);
			} catch (NumberFormatException e) {
				// ignore it
			}
		}
	}

	public void setName(String name) {
		m_name = name;
	}

	public void setPage(ReportPage page) {
		m_page = page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TRANSACTION);
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
