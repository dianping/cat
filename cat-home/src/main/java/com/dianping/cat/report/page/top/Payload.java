package com.dianping.cat.report.page.top;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	public Payload() {
		super(ReportPage.TOP);
	}

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("minute")
	private String m_minute;

	@FieldMeta("count")
	private int m_minuteCounts = 8;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("tops")
	private int m_topCounts = 11;

	@FieldMeta("refresh")
	private boolean m_refresh = false;

	@FieldMeta("fullScreen")
	private boolean m_fullScreen = false;

	public String getMinute() {
		return m_minute;
	}

	public void setMinute(String minute) {
		m_minute = minute;
	}

	public int getMinuteCounts() {
		return m_minuteCounts;
	}

	public void setMinuteCounts(int minuteCounts) {
		m_minuteCounts = minuteCounts;
	}

	public int getFrequency() {
		return m_frequency;
	}

	public void setFrequency(int frequency) {
		m_frequency = frequency;
	}

	public int getTopCounts() {
		return m_topCounts;
	}

	public void setTopCounts(int topCounts) {
		m_topCounts = topCounts;
	}

	public boolean isRefresh() {
		return m_refresh;
	}

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	public boolean isFullScreen() {
		return m_fullScreen;
	}

	public void setFullScreen(boolean fullScreen) {
		m_fullScreen = fullScreen;
	}

	public long getCurrentTimeMillis() {
		return System.currentTimeMillis();
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TOP);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
