package com.dianping.cat.report.page.database;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("product")
	private String m_product;

	@FieldMeta("frequency")
	private int m_frequency = 10;

	@FieldMeta("refresh")
	private boolean m_refresh = false;

	@FieldMeta("timeRange")
	private int m_timeRange = 24;

	@FieldMeta("fullScreen")
	private boolean m_fullScreen = false;

	@FieldMeta("hideNav")
	private boolean m_hideNav = true;

	@FieldMeta("group")
	private String m_group;

	@FieldMeta("minute")
	private int m_minute = -1;

	@FieldMeta("operations")
	private String m_operations;

	@FieldMeta("project")
	private String m_project;

	public Payload() {
		super(ReportPage.DATABASE);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public int getFrequency() {
		return m_frequency;
	}

	public String getGroup() {
		return m_group;
	}

	public int getMinute() {
		return m_minute;
	}

	public String getOperations() {
		return m_operations;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getProduct() {
		return m_product;
	}

	public String getProject() {
		return m_project;
	}

	public int getTimeRange() {
		return m_timeRange;
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
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setFrequency(int frequency) {
		m_frequency = frequency;
	}

	public void setFullScreen(boolean fullScreen) {
		m_fullScreen = fullScreen;
	}

	public void setGroup(String group) {
		m_group = group;
	}

	public void setHideNav(boolean hideNav) {
		m_hideNav = hideNav;
	}

	public void setMinute(int minute) {
		m_minute = minute;
	}

	public void setOperations(String operations) {
		m_operations = operations;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.NETWORK);
	}

	public void setProduct(String product) {
		m_product = product;
	}

	public void setProject(String project) {
		m_project = project;
	}

	public void setRefresh(boolean refresh) {
		m_refresh = refresh;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}

}
