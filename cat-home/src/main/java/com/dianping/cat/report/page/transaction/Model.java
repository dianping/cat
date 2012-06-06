package com.dianping.cat.report.page.transaction;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

public class Model extends AbstractReportModel<Action, Context> {
	private TransactionReport m_report;
	
	private DisplayTransactionTypeReport m_displayTypeReport;
	
	private DisplayTransactionNameReport m_displayNameReport;

	private String m_type;

	private String m_graph1;

	private String m_graph2;

	private String m_graph3;

	private String m_graph4;
	
	private String m_mobileResponse;
	
	private String m_queryName;
	
	private String m_urlResponseTrend;
	
	private String m_urlHitTrend;
	
	private String m_callResponseTrend;

	private String m_callHitTrend;
	
	private String m_sqlResponseTrend;
	
	private String m_sqlHitTrend;
	
	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_REPORT;
	}

	@Override
   public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
   }

	// required by report tag
	@Override
	public List<String> getDomains() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getDomainNames());
		}
	}
	
	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getIps());
		}
	}
	
	public String getQueryName() {
		return m_queryName;
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
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

	public TransactionReport getReport() {
		return m_report;
	}

	public String getType() {
		return m_type;
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

	public DisplayTransactionTypeReport getDisplayTypeReport() {
   	return m_displayTypeReport;
   }

	public void setDisplayTypeReport(DisplayTransactionTypeReport dispalyReport) {
		m_displayTypeReport = dispalyReport;
   }

	public DisplayTransactionNameReport getDisplayNameReport() {
   	return m_displayNameReport;
   }

	public void setDisplayNameReport(DisplayTransactionNameReport displayNameReport) {
   	m_displayNameReport = displayNameReport;
   }

	public String getMobileResponse() {
   	return m_mobileResponse;
   }

	public void setMobileResponse(String mobileResponse) {
   	m_mobileResponse = mobileResponse;
   }

	public String getUrlResponseTrend() {
   	return m_urlResponseTrend;
   }

	public void setUrlResponseTrend(String urlResponseTrend) {
   	m_urlResponseTrend = urlResponseTrend;
   }

	public String getUrlHitTrend() {
   	return m_urlHitTrend;
   }

	public void setUrlHitTrend(String urlHitTrend) {
   	m_urlHitTrend = urlHitTrend;
   }

	public String getCallResponseTrend() {
   	return m_callResponseTrend;
   }

	public void setCallResponseTrend(String callResponseTrend) {
   	m_callResponseTrend = callResponseTrend;
   }

	public String getCallHitTrend() {
   	return m_callHitTrend;
   }

	public void setCallHitTrend(String callHitTrend) {
   	m_callHitTrend = callHitTrend;
   }

	public String getSqlResponseTrend() {
   	return m_sqlResponseTrend;
   }

	public void setSqlResponseTrend(String sqlResponseTrend) {
   	m_sqlResponseTrend = sqlResponseTrend;
   }

	public String getSqlHitTrend() {
   	return m_sqlHitTrend;
   }

	public void setSqlHitTrend(String sqlHitTrend) {
   	m_sqlHitTrend = sqlHitTrend;
   }
}
