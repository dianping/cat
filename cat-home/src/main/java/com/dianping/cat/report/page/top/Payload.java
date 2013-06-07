package com.dianping.cat.report.page.top;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("count")
	private int m_count = 10;

	@FieldMeta("second")
	private int m_second;

	@FieldMeta("tops")
	private int m_tops = 10;

	@FieldMeta("refresh")
	private boolean m_refresh;

	public int getSecond() {
		return m_second;
	}

	public void setSecond(int second) {
		m_second = second;
	}

	public Payload() {
		super(ReportPage.TOP);
	}

	public boolean getRefresh() {
		return m_refresh;
	}

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public int getCount() {
		return m_count;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setCount(int count) {
		m_count = count;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TOP);
	}

	public int getTops() {
		return m_tops;
	}

	public void setTops(int tops) {
		m_tops = tops;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
