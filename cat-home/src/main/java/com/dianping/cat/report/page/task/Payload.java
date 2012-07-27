package com.dianping.cat.report.page.task;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.task.TaskHelper;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ActionPayload;
import com.site.web.mvc.payload.annotation.FieldMeta;

public class Payload implements ActionPayload<ReportPage, Action> {
	
	protected static final long ONE_HOUR = 3600 * 1000L;
	
	private ReportPage m_page;

	@FieldMeta("op")
	private Action m_action;
	
	@FieldMeta("currentPage")
	private int currentPage;
	
	@FieldMeta("status")
	private int status;
	
	@FieldMeta("type")
	private int type;
	
	@FieldMeta("name")
	private String name;
	
	@FieldMeta("domain")
	private String domain;
	
	@FieldMeta("step")
	private int step;
	
	@FieldMeta("date")
	private long  date;
	
	@FieldMeta("reportType")
	private String reportType;
	
	
	@FieldMeta("taskID")
	private int taskID;
	
	public int getTaskID() {
   	return taskID;
   }

	public void setTaskID(int taskID) {
   	this.taskID = taskID;
   }

	public long getStartDate() {
   	return this.getDate();
   }

	public long getEndDate() {
		long start=this.getDate();
   	return start+ONE_HOUR*24;
   }

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

	private SimpleDateFormat m_dayFormat = new SimpleDateFormat("yyyyMMdd");
	

	public long getCurrentDate() {
		return TaskHelper.todayZero(new Date()).getTime();
	}

	public long getDate() {
		long current = getCurrentDate();

		long extra = step * ONE_HOUR;
		if (reportType != null
		      && (reportType.equals("day") || reportType.equals("month") || reportType.equals("week"))) {
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
	
	public void setDate(String date) {
		//default:today's task
		if (date == null || date.length() == 0) {
			this.date = getCurrentDate();
		} else {
			try {
				Date temp = null;
				if (date != null && date.length() == 10) {
					temp = m_dateFormat.parse(date);
				} else if(date != null && date.length() == 8){
					temp = m_dayFormat.parse(date);
				}else{
					temp=new Date(Long.parseLong(date));
				}
				this.date = TaskHelper.todayZero(temp).getTime();
			} catch (Exception e) {
				// ignore it
				this.date = getCurrentDate();
			}
		}
	}

	public void setAction(String action) {
		m_action = Action.getByName(action, Action.VIEW);
	}

	@Override
	public Action getAction() {
		if(m_action==null){
			m_action = Action.VIEW;
		}
		return m_action;
	}

	@Override
	public ReportPage getPage() {
		return m_page;
	}

	@Override
	public void setPage(String page) {
		m_page = ReportPage.getByName(page, ReportPage.TASK);
	}

	public int getCurrentPage() {
   	return currentPage;
   }

	public void setCurrentPage(int currentPage) {
   	this.currentPage = currentPage;
   }

	public int getStatus() {
   	return status;
   }

	public void setStatus(int status) {
   	this.status = status;
   }


	public String getName() {
   	return name;
   }

	public void setName(String name) {
   	this.name = name;
   }

	public String getDomain() {
   	return domain;
   }

	public void setDomain(String domain) {
   	this.domain = domain;
   }

	public int getType() {
   	return type;
   }

	public void setType(int type) {
   	this.type = type;
   }

	public void setPage(ReportPage page) {
   	m_page = page;
   }

	public int getStep() {
   	return step;
   }

	public void setStep(int step) {
   	this.step = step;
   }

	public String getReportType() {
   	return reportType;
   }

	public void setReportType(String reportType) {
   	this.reportType = reportType;
   }

	@Override
	public void validate(ActionContext<?> ctx) {
		if(m_action==null){
			m_action = Action.VIEW;
		}
	}
}
