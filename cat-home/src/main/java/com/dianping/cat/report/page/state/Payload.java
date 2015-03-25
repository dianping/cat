package com.dianping.cat.report.page.state;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("key")
	private String m_key;

	@FieldMeta("sort")
	private String m_sort;

	@FieldMeta("show")
	private boolean m_show = true;

	public Payload() {
		super(ReportPage.STATE);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getKey() {
		return m_key;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getSort() {
		return m_sort;
	}

	public boolean isShow() {
		return m_show;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.HOURLY);
	}

	public void setKey(String key) {
		m_key = key;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.STATE);
	}

	public void setShow(boolean show) {
		m_show = show;
	}

	public void setSort(String sort) {
		m_sort = sort;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.HOURLY;
		}
	}
}
