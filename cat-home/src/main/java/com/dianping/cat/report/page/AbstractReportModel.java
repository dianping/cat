package com.dianping.cat.report.page;

import java.util.Collection;
import java.util.Date;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.view.UrlNav;
import com.site.web.mvc.Action;
import com.site.web.mvc.ActionContext;
import com.site.web.mvc.ViewModel;

public abstract class AbstractReportModel<A extends Action, M extends ActionContext<?>> extends
      ViewModel<ReportPage, A, M> {
	private Throwable m_exception;

	private long m_date;

	public AbstractReportModel(M ctx) {
		super(ctx);
	}

	public String getBaseUri() {
		return buildPageUri(getPage().getPath(), null);
	}

	// required by report tag
	public Date getCurrentTime() {
		return new Date();
	}

	// required by report tag
	public long getDate() {
		return m_date;
	}

	// required by report tag
	public abstract String getDomain();
	
	// required by report tag
	public abstract Collection<String> getDomains();

	public Throwable getException() {
		return m_exception;
	}

	public String getLogViewBaseUri() {
		return buildPageUri(ReportPage.LOGVIEW.getPath(), null);
	}

	// required by report tag
	public UrlNav[] getNavs() {
		return UrlNav.values();
	}

	public void setDate(long date) {
		m_date = date;
	}

	public void setException(Throwable exception) {
		m_exception = exception;
	}
}
