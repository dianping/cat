package com.dianping.cat.report.page.dependency;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {
	@FieldMeta("minute")
	private String m_minute;

	@FieldMeta("all")
	private boolean m_all;

	private ReportPage m_page;

	@FieldMeta("range")
	private int m_range = 24;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("productLine")
	private String productLine;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("refresh")
	private boolean m_refresh = false;

	@FieldMeta("fullScreen")
	private boolean m_fullScreen = false;

	@FieldMeta("hideNav")
	private boolean m_hideNav = true;

	@FieldMeta("tab")
	private String m_tab = "tab1";

	public Payload() {
		super(ReportPage.DEPENDENCY);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public long getCurrentDate() {
		long timestamp = getCurrentTimeMillis();

		return timestamp - timestamp % TimeHelper.ONE_HOUR;
	}

	public long getCurrentTimeMillis() {
		return System.currentTimeMillis() - TimeHelper.ONE_MINUTE * 1;
	}

	public long getDate() {
		long current = getCurrentDate();
		long extra = m_step * TimeHelper.ONE_HOUR;

		if (m_date <= 0) {
			return current + extra;
		} else {
			long result = m_date + extra;

			if (result > current) {
				return current;
			}
			return result;
		}
	}

	public int getFrequency() {
		return m_frequency;
	}

	public String getMinute() {
		return m_minute;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getProductLine() {
		return productLine;
	}

	public int getRange() {
		return m_range;
	}

	public String getTab() {
		return m_tab;
	}

	public boolean isAll() {
		return m_all;
	}

	public boolean isFullScreen() {
		return m_fullScreen;
	}

	public boolean isHideNav() {
		return m_hideNav;
	}

	public boolean isRefresh() {
		return m_refresh;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.LINE_CHART);
	}

	public void setAll(boolean all) {
		this.m_all = all;
	}

	public void setFrequency(int frequency) {
		m_frequency = frequency;
	}

	public void setFullScreen(boolean fullScreen) {
		m_fullScreen = fullScreen;
	}

	public void setHideNav(boolean hideNav) {
		m_hideNav = hideNav;
	}

	public void setMinute(String minute) {
		this.m_minute = minute;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.DEPENDENCY);
	}

	public void setProductLine(String productLine) {
		this.productLine = productLine;
	}

	public void setRange(int range) {
		m_range = range;
	}

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	public void setTab(String tab) {
		m_tab = tab;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.LINE_CHART;
		}
	}

}
