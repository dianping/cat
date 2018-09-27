package com.dianping.cat.report.page.appstats;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.unidal.helper.Splitters;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.app.QueryType;

public class Payload extends AbstractReportPayload<Action, ReportPage> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("day")
	private String m_day;

	@FieldMeta("sort")
	private String m_sort = QueryType.NETWORK_SUCCESS.getName();

	@FieldMeta("top")
	private int m_top = 20;

	@FieldMeta("codes")
	private List<String> m_codes = Collections.emptyList();

	@FieldMeta("appId")
	private int m_appId = 1;

	@FieldMeta("type")
	private String m_type;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	public Payload() {
		super(ReportPage.APPSTATS);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public List<String> getCodes() {
		return m_codes;
	}

	public String getDay() {
		return m_day;
	}

	public Date getDayDate() {
		try {
			if (m_day.length() == 10) {
				return m_sdf.parse(m_day);
			} else {
				return TimeHelper.getYesterday();
			}
		} catch (Exception e) {
			return TimeHelper.getYesterday();
		}
	}

	public int getAppId() {
		return m_appId;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getSort() {
		return m_sort;
	}

	public int getTop() {
		return m_top;
	}

	public String getType() {
		return m_type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setCodes(String codes) {
		m_codes = Splitters.by(",").noEmptyItem().split(codes);
	}

	public void setNamespace(int namespace) {
		m_appId = namespace;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.APPSTATS);
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
