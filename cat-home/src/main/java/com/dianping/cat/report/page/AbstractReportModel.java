package com.dianping.cat.report.page;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.unidal.lookup.ContainerLoader;
import org.unidal.web.mvc.Action;
import org.unidal.web.mvc.ActionContext;
import org.unidal.web.mvc.ViewModel;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.view.DomainNavManager;
import com.dianping.cat.report.view.DomainNavManager.Department;
import com.dianping.cat.report.view.HistoryNav;
import com.dianping.cat.report.view.UrlNav;
import com.dianping.cat.service.HostinfoService;

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

	private DomainNavManager m_manager;

	private HostinfoService m_hostinfoService;

	public AbstractReportModel(M ctx) {
		super(ctx);
		try {
			m_manager = ContainerLoader.getDefaultContainer().lookup(DomainNavManager.class);
			m_hostinfoService = ContainerLoader.getDefaultContainer().lookup(HostinfoService.class);
		} catch (Exception e) {
			Cat.logError(e);
		}
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

	public String getDepartment() {
		String domain = getDomain();

		if (domain != null && m_manager != null) {
			Project project = m_manager.getProjectByName(domain);
			if (project != null) {
				return project.getDepartment();
			}
		}
		return "Default";
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
		} else {
			return Integer.toString(hour);
		}
	}

	public String getIpToHostnameStr() {
		return new JsonBuilder().toJson(getIpToHostname());
	}

	public abstract String getDomain();

	public Map<String, Department> getDomainGroups() {
		return m_manager.getDepartment(getDomains());
	}

	// required by report tag
	public abstract Collection<String> getDomains();

	public Throwable getException() {
		return m_exception;
	}

	// required by report tag
	// required by report history tag
	public HistoryNav[] getHistoryNavs() {
		return HistoryNav.values();
	}

	public String getIpAddress() {
		return m_ipAddress;
	}

	public List<String> getIps() {
		return new ArrayList<String>();
	};

	public Map<String, String> getIpToHostname() {
		List<String> ips = getIps();
		Map<String, String> ipToHostname = new HashMap<String, String>();

		for (String ip : ips) {
			String hostname = m_hostinfoService.queryHostnameByIp(ip);
			ipToHostname.put(ip, hostname);
		}

		return ipToHostname;
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

	public String getProjectLine() {
		String domain = getDomain();

		if (domain != null && m_manager != null) {
			Project project = m_manager.getProjectByName(domain);
			if (project != null) {
				return project.getProjectLine();
			}
		}
		return "Default";
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
