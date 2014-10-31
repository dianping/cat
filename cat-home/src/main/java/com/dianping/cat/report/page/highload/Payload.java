package com.dianping.cat.report.page.highload;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.ReportPage;

public class Payload implements ActionPayload<ReportPage, Action> {
	private ReportPage m_page;

	private DateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd");

	@FieldMeta("op")
	private Action m_action;

	@FieldMeta("date")
	private String m_date;

	@FieldMeta("reportType")
	private String m_reportType = "";

	@Override
	public Action getAction() {
		return m_action;
	}

	public Date getDate() {
		try {
			if (m_date.length() == 10) {
				return m_sdf.parse(m_date);
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

	public String getReportType() {
		return m_reportType;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setDate(String date) {
		m_date = date;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.HIGHLOAD);
	}

	public void setReportType(String reportType) {
		m_reportType = reportType;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
