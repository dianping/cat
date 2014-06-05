package com.dianping.cat.report.page.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.unidal.web.mvc.view.annotation.EntityMeta;

import com.dianping.cat.Constants;
import com.dianping.cat.home.alert.report.entity.AlertReport;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.transform.DefaultJsonBuilder;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;
import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.report.page.AbstractReportModel;

public class Model extends AbstractReportModel<Action, Context> {

	private String m_browserChart;

	private String m_osChart;

	@EntityMeta
	private BugReport m_bugReport;

	@EntityMeta
	private ServiceReport m_serviceReport;

	@EntityMeta
	private HeavyReport m_heavyReport;
	
	@EntityMeta
	private AlertReport m_alertReport;

	@EntityMeta
	private UtilizationReport m_utilizationReport;

	private List<Domain> m_serviceList;

	private List<Url> m_callUrls;

	private List<Service> m_callServices;

	private List<Url> m_sqlUrls;

	private List<Service> m_sqlServices;

	private List<Url> m_cacheUrls;

	private List<Service> m_cacheServices;

	private Map<String, ErrorStatis> m_errorStatis;

	private List<com.dianping.cat.home.utilization.entity.Domain> m_utilizationWebList;

	private List<com.dianping.cat.home.utilization.entity.Domain> m_utilizationServiceList;
	
	private List<com.dianping.cat.home.alert.report.entity.Domain> m_alertDomains;
	
	private List<com.dianping.cat.home.alert.report.entity.Exception> m_alertExceptions;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getBrowserChart() {
		return m_browserChart;
	}

	public BugReport getBugReport() {
		return m_bugReport;
	}

	public String getBugs() {
		return new DefaultJsonBuilder().build(m_bugReport);
	}

	public List<Service> getCacheServices() {
		return m_cacheServices;
	}

	public List<Url> getCacheUrls() {
		return m_cacheUrls;
	}

	public List<Service> getCallServices() {
		return m_callServices;
	}

	public List<Url> getCallUrls() {
		return m_callUrls;
	}

	@Override
	public Action getDefaultAction() {
		return Action.BUG_REPORT;
	}

	@Override
	public String getDomain() {
		return Constants.CAT;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}

	public Map<String, ErrorStatis> getErrorStatis() {
		return m_errorStatis;
	}
	
	public HeavyReport getHeavyReport() {
		return m_heavyReport;
	}
	
	public AlertReport getAlertReport() {
		return m_alertReport;
	}

	public String getOsChart() {
		return m_osChart;
	}

	public List<com.dianping.cat.home.service.entity.Domain> getServiceList() {
		return m_serviceList;
	}

	public ServiceReport getServiceReport() {
		return m_serviceReport;
	}

	public List<Service> getSqlServices() {
		return m_sqlServices;
	}

	public List<Url> getSqlUrls() {
		return m_sqlUrls;
	}

	public UtilizationReport getUtilizationReport() {
		return m_utilizationReport;
	}

	public List<com.dianping.cat.home.utilization.entity.Domain> getUtilizationServiceList() {
		return m_utilizationServiceList;
	}

	public List<com.dianping.cat.home.utilization.entity.Domain> getUtilizationWebList() {
		return m_utilizationWebList;
	}

	public void setBrowserChart(String browserChart) {
		m_browserChart = browserChart;
	}

	public void setBugReport(BugReport bugReport) {
		m_bugReport = bugReport;
	}

	public void setCacheServices(List<Service> cacheServices) {
		m_cacheServices = cacheServices;
	}

	public void setCacheUrls(List<Url> cacheUrls) {
		m_cacheUrls = cacheUrls;
	}

	public void setCallServices(List<Service> callServices) {
		m_callServices = callServices;
	}

	public void setCallUrls(List<Url> callUrls) {
		m_callUrls = callUrls;
	}

	public void setErrorStatis(Map<String, ErrorStatis> errorStatis) {
		m_errorStatis = errorStatis;
	}

	public void setHeavyReport(HeavyReport heavyReport) {
		m_heavyReport = heavyReport;
	}
	
	public void setAlertReport(AlertReport alertReport) {
		m_alertReport = alertReport;
	}

	public void setOsChart(String osChart) {
		m_osChart = osChart;
	}

	public void setServiceList(List<com.dianping.cat.home.service.entity.Domain> serviceList) {
		this.m_serviceList = serviceList;
	}

	public void setServiceReport(ServiceReport serviceReport) {
		m_serviceReport = serviceReport;
	}

	public void setSqlServices(List<Service> sqlServices) {
		m_sqlServices = sqlServices;
	}

	public void setSqlUrls(List<Url> sqlUrls) {
		m_sqlUrls = sqlUrls;
	}

	public void setUtilizationReport(UtilizationReport utilizationReport) {
		m_utilizationReport = utilizationReport;
	}

	public void setUtilizationServiceList(List<com.dianping.cat.home.utilization.entity.Domain> utilizationServiceList) {
		m_utilizationServiceList = utilizationServiceList;
	}

	public void setUtilizationWebList(List<com.dianping.cat.home.utilization.entity.Domain> utilizationWebList) {
		m_utilizationWebList = utilizationWebList;
	}

	public List<com.dianping.cat.home.alert.report.entity.Domain> getAlertDomains() {
		return m_alertDomains;
	}

	public void setAlertDomains(List<com.dianping.cat.home.alert.report.entity.Domain> alertDomains) {
		m_alertDomains = alertDomains;
	}

	public List<com.dianping.cat.home.alert.report.entity.Exception> getAlertExceptions() {
		return m_alertExceptions;
	}

	public void setAlertExceptions(List<com.dianping.cat.home.alert.report.entity.Exception> alertExceptions) {
		m_alertExceptions = alertExceptions;
	}

}
