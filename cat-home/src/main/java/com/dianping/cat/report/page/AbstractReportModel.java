package com.dianping.cat.report.page;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ViewModel;

import com.dainping.cat.consumer.core.dal.Project;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.report.view.DomainNavManager.Department;
import com.dianping.cat.report.view.HistoryNav;
import com.dianping.cat.report.view.UrlNav;

public abstract class AbstractReportModel<A extends Action, M extends ActionContext<?>> extends
      ViewModel<ReportPage, A, M> {
	private Date m_creatTime;

	private String m_customDate;

	private long m_date;

	private SimpleDateFormat m_dateFormat = new SimpleDateFormat("yyyyMMddHH");

	private SimpleDateFormat m_dayFormat = new SimpleDateFormat("yyyyMMdd");

	private String m_displayDomain;

	private Throwable m_exception;

	private String m_ipAddress;

	private String m_reportType;

	public AbstractReportModel(M ctx) {
		super(ctx);
	}

	public String getBaseUri() {
		return buildPageUri(getPage().getPath(), null);
	}

	public Date getCreatTime() {
		return m_creatTime;
	}

	// required by current tag()
	public HistoryNav getCurrentNav() {
		return HistoryNav.getByName(m_reportType);
	}

	// required by report tag
	public Date getCurrentTime() {
		return new Date();
	}

	public String getCustomDate() {
		return m_customDate;
	}

	// required by report tag
	public String getDate() {
		if (m_reportType != null && m_reportType.length() > 0) {
			return m_dayFormat.format(new Date(m_date));
		}
		return m_dateFormat.format(new Date(m_date));
	}

	public String getDisplayDomain() {
		return m_displayDomain;
	}

	public String getDisplayHour() {
		Calendar cal = Calendar.getInstance();
		cal.setTimeInMillis(m_date);
		int hour = cal.get(Calendar.HOUR_OF_DAY);
		if (hour < 10) {
			return "0" + Integer.toString(hour);
		} else
			return Integer.toString(hour);
	}

	// required by report tag
	public abstract String getDomain();

	// required by report tag
	public abstract Collection<String> getDomains();

	public String getDepartment() {
		String domain = getDomain();
		
		if (domain != null) {
			Project project = DomainNavManager.getProjectByName(domain);
			if (project != null) {
				return project.getDepartment();
			}
		}
		return "Default";
	}

	public String getProjectLine() {
		String domain = getDomain();
		
		if (domain != null) {
			Project project = DomainNavManager.getProjectByName(domain);
			if (project != null) {
				return project.getProjectLine();
			}
		}
		return "Default";
	}
	
	public Map<String, Department> getDomainGroups() {
		return DomainNavManager.getDepartment(getDomains());
	}

	public Throwable getException() {
		return m_exception;
	}

	// required by report history tag
	public HistoryNav[] getHistoryNavs() {
		return HistoryNav.values();
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public String getLogViewBaseUri() {
		return buildPageUri(ReportPage.LOGVIEW.getPath(), null);
	}

	public long getLongDate() {
		return m_date;
	}

	// required by report tag
	public UrlNav[] getNavs() {
		return UrlNav.values();
	}

	public String getReportType() {
		return m_reportType;
	}

	public void setCreatTime(Date creatTime) {
		m_creatTime = creatTime;
	}

	public void setCustomDate(Date start, Date end) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		StringBuilder sb = new StringBuilder();

		sb.append("&startDate=").append(sdf.format(start)).append("&endDate=").append(sdf.format(end));
		m_customDate = sb.toString();
	}

	public void setDisplayDomain(String displayDomain) {
		m_displayDomain = displayDomain;
	}

	public void setException(Throwable exception) {
		m_exception = exception;
	}

	public void setIpAddress(String ipAddress) {
		m_ipAddress = ipAddress;
	}

	public void setLongDate(long date) {
		m_date = date;
	}

	public void setReportType(String reportType) {
		m_reportType = reportType;
	}
}
