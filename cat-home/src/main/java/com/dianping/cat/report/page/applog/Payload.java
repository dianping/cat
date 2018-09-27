package com.dianping.cat.report.page.applog;

import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.applog.service.AppLogQueryEntity;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

public class Payload extends AbstractReportPayload<Action, ReportPage> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@ObjectMeta("appLogQuery")
	private AppLogQueryEntity m_appLogQuery = new AppLogQueryEntity();

	@FieldMeta("id")
	private int m_id;

	public Payload() {
		super(ReportPage.APPLOG);
	}

	public int getId() {
		return m_id;
	}

	public void setId(int id) {
		m_id = id;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.APP_LOG);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public AppLogQueryEntity getAppLogQuery() {
		return m_appLogQuery;
	}

	public void setAppLogQuery(AppLogQueryEntity appLogQuery) {
		m_appLogQuery = appLogQuery;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.APPLOG);
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.APP_LOG;
		}
	}
}
