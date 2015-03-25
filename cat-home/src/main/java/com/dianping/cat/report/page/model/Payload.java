package com.dianping.cat.report.page.model;

import java.util.Arrays;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.PathMeta;

import com.dianping.cat.mvc.ApiPayload;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.service.ModelPeriod;

public class Payload extends ApiPayload implements ActionPayload<ReportPage, Action> {
	@FieldMeta("op")
	private Action m_action;

	private ReportPage m_page;

	@PathMeta("path")
	private String[] m_path;

	@Override
	public Action getAction() {
		return m_action;
	}

	public String getDomain() {
		if (m_path.length > 1) {
			return m_path[1];
		} else {
			return null;
		}
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public ModelPeriod getPeriod() {
		if (m_path.length > 2) {
			return ModelPeriod.getByName(m_path[2], ModelPeriod.CURRENT);
		} else {
			return ModelPeriod.CURRENT;
		}
	}

	public String getReport() {
		if (m_path.length > 0) {
			return m_path[0];
		} else {
			return null;
		}
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.XML);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.MODEL);
	}

	public void setPath(String[] path) {
		if (path == null) {
			m_path = new String[0];
		} else {
			m_path = Arrays.copyOf(path, path.length);
		}
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.XML;
		}
	}
}
