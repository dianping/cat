package com.dianping.cat.report.page.activity;

import java.text.SimpleDateFormat;
import java.util.Calendar;
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

	@FieldMeta("startTime")
	private String m_startTime;

	@FieldMeta("endTime")
	private String m_endTime;

	public Payload() {
		super(ReportPage.ACTIVITY);
	}

	@Override
	public Action getAction() {
		return m_action;
	}

	public Date getEndDate() {
		if (m_endTime != null) {
			return parseDate(m_endTime);
		} else {
			return TimeHelper.getCurrentHour(1);
		}
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public Date getStartDate() {
		if (m_startTime != null) {
			return parseDate(m_startTime);
		} else {
			return TimeHelper.getCurrentHour(-1);
		}
	}

	public Date parseDate(String date) {
		Date d = null;

		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");

			d = sdf.parse(date);
		} catch (Exception e) {
			d = new Date();
		}
		if (d.getTime() > System.currentTimeMillis()) {
			d = new Date();
		}

		Calendar cal = Calendar.getInstance();

		cal.setTime(d);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setEndTime(String endTime) {
		m_endTime = endTime;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.ACTIVITY);
	}

	public void setStartTime(String startTime) {
		m_startTime = startTime;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
