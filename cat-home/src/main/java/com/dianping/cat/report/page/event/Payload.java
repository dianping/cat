package com.dianping.cat.report.page.event;

import java.net.URLEncoder;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("show")
	private boolean m_showAll = false;

	@FieldMeta("sort")
	private String m_sortBy;

	@FieldMeta("type")
	private String m_type;

	@FieldMeta("group")
	private String m_group;

	public Payload() {
		super(ReportPage.EVENT);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getEncodedType() {
		try {
			return URLEncoder.encode(m_type, "utf-8");
		} catch (Exception e) {
			return m_type;
		}
	}

	public String getGroup() {
		return m_group;
	}

	public String getName() {
		return m_name;
	}

	public String getSortBy() {
		return m_sortBy;
	}

	public String getType() {
		return m_type;
	}

	public boolean isShowAll() {
		return m_showAll;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY_REPORT);
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.EVENT);
	}

	public void setShowAll(boolean showAll) {
		m_showAll = showAll;
	}

	public void setSortBy(String sortBy) {
		m_sortBy = sortBy;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY_REPORT;
		}
	}

}
