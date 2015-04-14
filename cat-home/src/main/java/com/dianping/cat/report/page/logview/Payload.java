package com.dianping.cat.report.page.logview;

import java.util.Arrays;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.PathMeta;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {
	@FieldMeta("op")
	private Action m_action = Action.VIEW;

	@PathMeta("path")
	private String[] m_path;

	@FieldMeta("header")
	private boolean m_showHeader = true;

	@FieldMeta("waterfall")
	private boolean m_waterfall = false;

	public Payload() {
		super(ReportPage.LOGVIEW);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public String[] getPath() {
		return m_path;
	}

	public boolean isShowHeader() {
		return m_showHeader;
	}

	public boolean isWaterfall() {
		return m_waterfall;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, m_action);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.LOGVIEW);
	}

	public void setPath(String[] path) {
		if (path == null) {
			m_path = new String[0];
		} else {
			m_path = Arrays.copyOf(path, path.length);
		}
	}

	public void setShowHeader(String showHeader) {
		m_showHeader = !"no".equals(showHeader);
	}

	public void setWaterfall(boolean waterfall) {
		m_waterfall = waterfall;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
	}
}
