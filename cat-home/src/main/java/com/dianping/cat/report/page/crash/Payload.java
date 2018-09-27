package com.dianping.cat.report.page.crash;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.crash.display.SortType;
import com.dianping.cat.report.page.crash.service.CrashLogQueryEntity;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;
import org.unidal.web.mvc.payload.annotation.ObjectMeta;

public class Payload extends AbstractReportPayload<Action, ReportPage> {

	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@ObjectMeta("crashLogQuery")
	private CrashLogQueryEntity m_crashLogQuery = new CrashLogQueryEntity();

	@FieldMeta("query1")
	private String m_query1;

	@FieldMeta("query2")
	private String m_query2;

	@FieldMeta("id")
	private int m_id;

	@FieldMeta("day")
	private String m_day;

	@FieldMeta("appId")
	private int m_appId = 1;

	@FieldMeta("platform")
	private int m_platform = 1;

	@FieldMeta("sort")
	private String m_sort = SortType.COUNT.getName();

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	public Payload() {
		super(ReportPage.CRASH);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public int getAppId() {
		return m_appId;
	}

	public CrashLogQueryEntity getCrashLogQuery() {
		return m_crashLogQuery;
	}

	public CrashLogQueryEntity getCrashLogTrendQuery1() {
		if (m_query1 != null && m_query1.length() > 0) {
			return new CrashLogQueryEntity(m_query1);
		} else {
			return new CrashLogQueryEntity();
		}
	}

	public CrashLogQueryEntity getCrashLogTrendQuery2() {
		if (m_query2 != null && m_query2.length() > 0) {
			return new CrashLogQueryEntity(m_query2);
		} else {
			return null;
		}
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

	public int getId() {
		return m_id;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public int getPlatform() {
		return m_platform;
	}

	public String getQuery1() {
		return m_query1;
	}

	public String getQuery2() {
		return m_query2;
	}

	public String getSort() {
		return m_sort;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.APP_CRASH_LOG);
	}

	public void setAppId(int appId) {
		m_appId = appId;
	}

	public void setCrashLogQuery(CrashLogQueryEntity crashLogQuery) {
		m_crashLogQuery = crashLogQuery;
	}

	public void setId(int id) {
		m_id = id;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.CRASH);
	}

	public void setQuery1(String query1) {
		m_query1 = query1;
	}

	public void setQuery2(String query2) {
		m_query2 = query2;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.APP_CRASH_LOG;
		}
	}
}
