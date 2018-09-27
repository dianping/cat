package com.dianping.cat.report.page.app;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.app.AppDataField;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.app.service.CommandQueryEntity;
import com.dianping.cat.report.page.app.service.DailyCommandQueryEntity;
import com.dianping.cat.report.page.app.service.SpeedQueryEntity;

public class Payload extends AbstractReportPayload<Action, ReportPage> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("query1")
	private String m_query1;

	@FieldMeta("query2")
	private String m_query2;

	@FieldMeta("type")
	private String m_type = QueryType.REQUEST.getName();

	@FieldMeta("groupByField")
	private AppDataField m_groupByField = AppDataField.CODE;

	@FieldMeta("sort")
	private String m_sort = QueryType.NETWORK_SUCCESS.getName();

	@FieldMeta("codeId")
	private int m_codeId;

	@FieldMeta("status")
	private String m_status;

	@FieldMeta("name")
	private String m_name;

	@FieldMeta("title")
	private String m_title;

	@FieldMeta("domains")
	private String m_domains;

	@FieldMeta("commandId")
	private String m_commandId;

	@FieldMeta("domains2")
	private String m_domains2;

	@FieldMeta("commandId2")
	private String m_commandId2;

	@FieldMeta("top")
	private int m_top = 20;

	@FieldMeta("appId")
	private int m_appId = 1;

	@FieldMeta("appId2")
	private int m_appId2 = 1;

	public Payload() {
		super(ReportPage.APP);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public int getAppId() {
		return m_appId;
	}

	public int getAppId2() {
		return m_appId2;
	}

	public int getCodeId() {
		return m_codeId;
	}

	public DailyCommandQueryEntity getCommandDailyQueryEntity() {
		if (m_query1 != null && m_query1.length() > 0) {
			return new DailyCommandQueryEntity(m_query1);
		} else {
			return new DailyCommandQueryEntity();
		}
	}

	public String getCommandId() {
		return m_commandId;
	}

	public String getCommandId2() {
		return m_commandId2;
	}

	public CommandQueryEntity getDashBoardQuery() {
		if (m_query1 != null && m_query1.length() > 0) {
			return new CommandQueryEntity(m_query1);
		} else {
			return new CommandQueryEntity(0);
		}
	}

	public String getDomains() {
		return m_domains;
	}

	public String getDomains2() {
		return m_domains2;
	}

	public AppDataField getGroupByField() {
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

	public CommandQueryEntity getQueryEntity1() {
		if (m_query1 != null && m_query1.length() > 0) {
			return new CommandQueryEntity(m_query1);
		} else {
			return new CommandQueryEntity();
		}
	}

	public CommandQueryEntity getQueryEntity2() {
		if (m_query2 != null && m_query2.length() > 0) {
			return new CommandQueryEntity(m_query2);
		} else {
			return null;
		}
	}

	public QueryType getQueryType() {
		return QueryType.findByName(m_type);
	}

	public String getSort() {
		return m_sort;
	}

	public SpeedQueryEntity getSpeedQueryEntity1() {
		if (m_query1 != null && m_query1.length() > 0) {
			return new SpeedQueryEntity(m_query1);
		} else {
			return new SpeedQueryEntity();
		}
	}

	public SpeedQueryEntity getSpeedQueryEntity2() {
		if (m_query2 != null && m_query2.length() > 0) {
			return new SpeedQueryEntity(m_query2);
		} else {
			return null;
		}
	}

	public String getStatus() {
		return m_status;
	}

	public String getTitle() {
		return m_title;
	}

	public int getTop() {
		return m_top;
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.LINECHART);
	}

	public void setAppId(int appId) {
		m_appId = appId;
	}

	public void setAppId2(int appId2) {
		m_appId2 = appId2;
	}

	public void setCodeId(int codeId) {
		m_codeId = codeId;
	}

	public void setCommandId(String commandId) {
		this.m_commandId = commandId;
	}

	public void setCommandId2(String commandId2) {
		this.m_commandId2 = commandId2;
	}

	public void setDomains(String domains) {
		m_domains = domains;
	}

	public void setDomains2(String domains2) {
		m_domains2 = domains2;
	}

	public void setGroupByField(String groupByField) {
		m_groupByField = AppDataField.getByName(groupByField, AppDataField.CODE);
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

	public void setSort(String sort) {
		m_sort = sort;
	}

	public void setStatus(String status) {
		m_status = status;
	}

	public void setTitle(String title) {
		m_title = title;
	}

	public void setTop(int top) {
		m_top = top;
	}

	public void setType(String type) {
		m_type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.LINECHART;
		}
	}
}
