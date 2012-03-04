package com.dianping.cat.report.page.transaction;

import java.util.Collections;
import java.util.Date;
import java.util.Set;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.view.UrlNav;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	private TransactionReport m_report;

	private String m_type;

	private Throwable m_exception;

	private String m_graph1;

	private String m_graph2;

	private String m_graph3;

	private String m_graph4;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getBaseUri() {
		return buildPageUri(getPage().getPath(), null);
	}

	// required by report tag
	public Date getCurrentTime() {
		return new Date();
	}

	@Override
	public Action getDefaultAction() {
		return Action.VIEW;
	}

	// required by report tag
	public Set<String> getDomains() {
		if (m_report == null) {
			return Collections.emptySet();
		} else {
			return m_report.getDomains();
		}
	}

	public Throwable getException() {
		return m_exception;
	}

	public String getGraph1() {
		return m_graph1;
	}

	public String getGraph2() {
   	return m_graph2;
   }

	public String getGraph3() {
   	return m_graph3;
   }

	public String getGraph4() {
   	return m_graph4;
   }

	public String getLogViewBaseUri() {
		return buildPageUri(ReportPage.LOGVIEW.getPath(), null);
	}

	// required by report tag
	public UrlNav[] getNavs() {
		return UrlNav.values();
	}

	public TransactionReport getReport() {
		return m_report;
	}

	public String getType() {
		return m_type;
	}

	public void setException(Throwable exception) {
		m_exception = exception;
	}

	public void setGraph1(String graph1) {
		m_graph1 = graph1;
	}

	public void setGraph2(String graph2) {
   	m_graph2 = graph2;
   }

	public void setGraph3(String graph3) {
   	m_graph3 = graph3;
   }

	public void setGraph4(String graph4) {
   	m_graph4 = graph4;
   }

	public void setReport(TransactionReport report) {
		m_report = report;
	}

	public void setType(String type) {
		m_type = type;
	}
}
