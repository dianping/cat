package com.dianping.cat.report.page.app;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.config.app.AppDataGroupByField;
import com.dianping.cat.config.app.AppDataService;
import com.dianping.cat.config.app.QueryEntity;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.AbstractReportPayload;

public class Payload extends AbstractReportPayload<Action> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("query1")
	private String m_query1;

	@FieldMeta("query2")
	private String m_query2;

	@FieldMeta("type")
	private String m_type = AppDataService.REQUEST;

	@FieldMeta("groupByField")
	private AppDataGroupByField m_groupByField = AppDataGroupByField.CODE;

	@FieldMeta("sort")
	private String m_sort = AppDataService.SUCCESS;

	@FieldMeta("showActivity")
	private boolean m_showActivity;
	
	@FieldMeta("name")
	private String m_name;
	
	@FieldMeta("title")
	private String m_title;

	public Payload() {
		super(ReportPage.APP);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public AppDataGroupByField getGroupByField() {
		return m_groupByField;
	}

	public String getName() {
		return m_name;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getQuery1() {
		return m_query1;
	}

	public String getQuery2() {
		return m_query2;
	}

	public QueryEntity getQueryEntity1() {
		if (m_query1 != null && m_query1.length() > 0) {
			return new QueryEntity(m_query1);
		} else {
			return new QueryEntity();
		}
	}

	public QueryEntity getQueryEntity2() {
		if (m_query2 != null && m_query2.length() > 0) {
			return new QueryEntity(m_query2);
		} else {
			return null;
		}
	}

	public String getSort() {
		return m_sort;
	}

	public String getTitle() {
		return m_title;
	}

	public String getType() {
		return m_type;
	}

	public boolean isShowActivity() {
		return m_showActivity;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setGroupByField(String groupByField) {
		m_groupByField = AppDataGroupByField.getByName(groupByField, AppDataGroupByField.CODE);
	}

	public void setName(String name) {
		m_name = name;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.APP);
	}

	public void setQuery1(String query1) {
		m_query1 = query1;
	}

	public void setQuery2(String query2) {
		m_query2 = query2;
	}

	public void setShowActivity(boolean showActivity) {
		m_showActivity = showActivity;
	}

	public void setSort(String sort) {
		m_sort = sort;
	}

	public void setTitle(String title) {
		m_title = title;
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
