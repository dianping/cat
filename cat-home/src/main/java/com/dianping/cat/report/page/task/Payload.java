package com.dianping.cat.report.page.task;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ActionPayload;
import org.unidal.web.mvc.payload.annotation.FieldMeta;

import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.task.TaskHelper;

public class Payload implements ActionPayload<ReportPage, Action> {

	@FieldMeta("currentPage")
	private int currentPage;

	@FieldMeta("date")
	private long date;

	@FieldMeta("domain")
	private String domain;

	@FieldMeta("op")
	private Action m_action;

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

	private SimpleDateFormat m_dayFormat = new SimpleDateFormat("yyyyMMdd");

	private ReportPage m_page;

	@FieldMeta("name")
	private String name;

	@FieldMeta("reportType")
	private String reportType;

	@FieldMeta("status")
	private int status;

	@FieldMeta("step")
	private int step;

	@FieldMeta("taskID")
	private int taskID;

	@FieldMeta("type")
	private int type;

	@Override
	public Action getAction() {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
		return m_action;
	}

	public long getCurrentDate() {
		return TaskHelper.todayZero(new Date()).getTime();
	}

	public int getCurrentPage() {
		return currentPage;
	}

	public long getDate() {
		long current = getCurrentDate();

		long extra = step * TimeUtil.ONE_HOUR;
		if (reportType != null && (reportType.equals("day") || reportType.equals("month") || reportType.equals("week"))) {
			extra = 0;
		}
		if (date <= 0) {
			return current + extra;
		} else {
			long result = date + extra;

			if (result > current) {
				return current;
			}
			return result;
		}
	}

	public String getDomain() {
		return domain;
	}

	public long getEndDate() {
		long start = this.getDate();
		return start + TimeUtil.ONE_HOUR * 24;
	}

	public String getName() {
		return name;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	public String getReportType() {
		return reportType;
	}

	public long getStartDate() {
		return this.getDate();
	}

	public int getStatus() {
		return status;
	}

	public int getStep() {
		return step;
	}

	public int getTaskID() {
		return taskID;
	}

	public int getType() {
		return type;
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	public void setCurrentPage(int currentPage) {
		this.currentPage = currentPage;
	}

	public void setDate(String date) {
		// default:today's task
		if (date == null || date.length() == 0) {
			this.date = getCurrentDate();
		} else {
			try {
				Date temp = null;
				if (date != null && date.length() == 10) {
					temp = m_dateFormat.parse(date);
				} else if (date != null && date.length() == 8) {
					temp = m_dayFormat.parse(date);
				} else {
					temp = new Date(Long.parseLong(date));
				}
				this.date = TaskHelper.todayZero(temp).getTime();
			} catch (Exception e) {
				// ignore it
				this.date = getCurrentDate();
			}
		}
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setPage(ReportPage page) {
		m_page = page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TASK);
	}

	public void setReportType(String reportType) {
		this.reportType = reportType;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public void setTaskID(int taskID) {
		this.taskID = taskID;
	}

	public void setType(int type) {
		this.type = type;
	}

	@Override
	public void validate(ActionContext<?> ctx) {
		if (m_action == null) {
			m_action = Action.VIEW;
		}
	}
}
