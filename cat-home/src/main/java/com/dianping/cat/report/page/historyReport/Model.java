package com.dianping.cat.report.page.historyReport;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.event.DisplayEventNameReport;
import com.dianping.cat.report.page.event.DisplayEventTypeReport;
import com.dianping.cat.report.page.problem.ProblemStatistics;
import com.dianping.cat.report.page.transaction.DisplayTransactionNameReport;
import com.dianping.cat.report.page.transaction.DisplayTransactionTypeReport;
import com.site.web.mvc.ViewModel;

public class Model extends ViewModel<ReportPage, Action, Context> {
	public String m_domain;

	public String m_ipAddress;
	
	public List<String> m_ips;
	
	public List<String> m_domains;

	private int m_threshold;
	
	private DisplayTransactionTypeReport m_transactionTypes;

	private DisplayTransactionNameReport m_transactionNames;

	private DisplayEventTypeReport m_eventTypes;

	private DisplayEventNameReport m_eventNames;

	private ProblemStatistics m_problemStatistics;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getDate() {
		return "";
	}

	@Override
	public Action getDefaultAction() {
		return Action.TRANSACTION;
	}

	public String getDomain() {
		return m_domain;
	}

	public List<String> getDomains() {
		List<String> result = new ArrayList<String>();
		result.add("MobileApi");
		result.add("Cat");
		return result;
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}

	public void setDomains(List<String> domains) {
		m_domains = domains;
	}

	public String getIpAddress() {
   	return m_ipAddress;
   }

	public void setIpAddress(String ip) {
   	m_ipAddress = ip;
   }

	public List<String> getIps() {
   	return m_ips;
   }

	public void setIps(List<String> ips) {
   	m_ips = ips;
   }

	public DisplayTransactionTypeReport getTransactionTypes() {
   	return m_transactionTypes;
   }

	public void setTransactionTypes(DisplayTransactionTypeReport transactionTypes) {
   	m_transactionTypes = transactionTypes;
   }

	public DisplayTransactionNameReport getTransactionNames() {
   	return m_transactionNames;
   }

	public void setTransactionNames(DisplayTransactionNameReport transactionNames) {
   	m_transactionNames = transactionNames;
   }

	public DisplayEventTypeReport getEventTypes() {
   	return m_eventTypes;
   }

	public void setEventTypes(DisplayEventTypeReport eventTypes) {
   	m_eventTypes = eventTypes;
   }

	public DisplayEventNameReport getEventNames() {
   	return m_eventNames;
   }

	public void setEventNames(DisplayEventNameReport eventNames) {
   	m_eventNames = eventNames;
   }

	public ProblemStatistics getProblemStatistics() {
   	return m_problemStatistics;
   }

	public void setProblemStatistics(ProblemStatistics problemStatistics) {
   	m_problemStatistics = problemStatistics;
   }
	
	public String getLogViewBaseUri() {
		return buildPageUri(ReportPage.LOGVIEW.getPath(), null);
	}

	public int getThreshold() {
   	return m_threshold;
   }

	public void setThreshold(int threshold) {
   	m_threshold = threshold;
   }

}
