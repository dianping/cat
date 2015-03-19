package com.dianping.cat.report.page.statistics;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.mvc.AbstractReportPayload;
import com.dianping.cat.report.ReportPage;

public class Payload extends AbstractReportPayload<Action,ReportPage> {
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("sort")
	private String m_sortBy = "avg";

	@FieldMeta("tab")
	private String m_tab = "tab1";

	@FieldMeta("summarydomain")
	private String m_summarydomain;

	@FieldMeta("summarytime")
	private String m_summarytime;

	@FieldMeta("summaryemails")
	private String m_summaryemails;

	@FieldMeta("day")
	private String m_day;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

	private SimpleDateFormat m_daySdf = new SimpleDateFormat("yyyy-MM-dd");

	public Payload() {
		super(ReportPage.STATISTICS);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public Date getDay() {
		try {
			if (m_day.length() == 10) {
				return m_daySdf.parse(m_day);
			} else {
				return TimeHelper.getYesterday();
			}
		} catch (Exception e) {
			return TimeHelper.getYesterday();
		}
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getSortBy() {
		return m_sortBy;
	}

	public String getSummarydomain() {
		if (m_summarydomain == null || "".equals(m_summarydomain)) {
			return null;
		} else {
			return m_summarydomain;
		}
	}

	public String getSummaryemails() {
		if (m_summaryemails == null || "".equals(m_summaryemails)) {
			return null;
		} else {
			return m_summaryemails;
		}
	}

	public Date getSummarytime() {
		try {
			return m_sdf.parse(m_summarytime);
		} catch (Exception ex) {
			return new Date();
		}
	}

	public String getTab() {
		return m_tab;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.BUG_REPORT);
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.STATISTICS);
	}

	public void setSortBy(String sortBy) {
		m_sortBy = sortBy;
	}

	public void setSummarydomain(String summaryDomain) {
		m_summarydomain = summaryDomain;
	}

	public void setSummaryemails(String summaryEmails) {
		m_summaryemails = summaryEmails;
	}

	public void setSummarytime(String summaryTime) {
		m_summarytime = summaryTime;
	}

	public void setTab(String tab) {
		m_tab = tab;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.BUG_REPORT;
		}
	}
}
