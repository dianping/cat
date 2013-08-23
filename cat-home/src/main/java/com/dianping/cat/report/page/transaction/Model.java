package com.dianping.cat.report.page.transaction;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.view.StringSortHelper;

@ModelMeta("transaction")
public class Model extends AbstractReportModel<Action, Context> {
	private DisplayNames m_displayNameReport;

	private DisplayTypes m_displayTypeReport;

	private String m_errorTrend;

	private String m_graph1;

	private String m_graph2;

	private String m_graph3;

	private String m_graph4;

	private String m_hitTrend;

	private String m_mobileResponse;

	private String m_queryName;

	@EntityMeta
	private TransactionReport m_report;

	private String m_responseTrend;

	private String m_type;
	
	private String m_pieChart;

	public Model(Context ctx) {
		super(ctx);
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_REPORT;
	}

	public DisplayNames getDisplayNameReport() {
		return m_displayNameReport;
	}

	public DisplayTypes getDisplayTypeReport() {
		return m_displayTypeReport;
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
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			return StringSortHelper.sortDomain(domainNames);
		}
	}
	
	public String getErrorTrend() {
		return m_errorTrend;
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

	public String getHitTrend() {
		return m_hitTrend;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return StringSortHelper.sortDomain(m_report.getIps());
		}
	}

	public String getMobileResponse() {
		return m_mobileResponse;
	}

	public String getPieChart() {
		return m_pieChart;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public TransactionReport getReport() {
		return m_report;
	}

	public String getResponseTrend() {
		return m_responseTrend;
	}

	public String getType() {
		return m_type;
	}

	public void setDisplayNameReport(DisplayNames displayNameReport) {
		m_displayNameReport = displayNameReport;
	}

	public void setDisplayTypeReport(DisplayTypes dispalyReport) {
		m_displayTypeReport = dispalyReport;
	}

	public void setErrorTrend(String errorTrend) {
		m_errorTrend = errorTrend;
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

	public void setHitTrend(String hitTrend) {
		m_hitTrend = hitTrend;
	}

	public void setMobileResponse(String mobileResponse) {
		m_mobileResponse = mobileResponse;
	}

	public void setPieChart(String pieChart) {
		m_pieChart = pieChart;
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public void setReport(TransactionReport report) {
		m_report = report;
	}

	public void setResponseTrend(String responseTrend) {
		m_responseTrend = responseTrend;
	}

	public void setType(String type) {
		m_type = type;
	}
	
}
