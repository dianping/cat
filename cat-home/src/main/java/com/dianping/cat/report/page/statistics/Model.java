package com.dianping.cat.report.page.statistics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.dianping.cat.Constants;
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

	private BugReport m_bugReport;

	private ServiceReport m_serviceReport;

	private HeavyReport m_heavyReport;

	private List<Domain> m_serviceList;

	private List<Url> m_callUrls;

	private List<Service> m_callServices;

	private List<Url> m_sqlUrls;

	private List<Service> m_sqlServices;

	private List<Url> m_cacheUrls;

	private List<Service> m_cacheServices;

	private Map<String, ErrorStatis> m_errorStatis;

	private UtilizationReport m_utilizationReport;

	private List<com.dianping.cat.home.utilization.entity.Domain> m_utilizationList;

	private List<com.dianping.cat.home.utilization.entity.Domain> m_utilizationWebList;

	private List<com.dianping.cat.home.utilization.entity.Domain> m_utilizationServiceList;

	public List<com.dianping.cat.home.utilization.entity.Domain> getUtilizationWebList() {
		return m_utilizationWebList;
	}

	public List<com.dianping.cat.home.utilization.entity.Domain> getUtilizationServiceList() {
		return m_utilizationServiceList;
	}

	public void setUtilizationServiceList(List<com.dianping.cat.home.utilization.entity.Domain> utilizationServiceList) {
		m_utilizationServiceList = utilizationServiceList;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	public BugReport getBugReport() {
		return m_bugReport;
	}

	public String getBugs() {
		return new DefaultJsonBuilder().buildJson(m_bugReport);
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

	public List<com.dianping.cat.home.utilization.entity.Domain> getUtilizationList() {
		return m_utilizationList;
	}

	public UtilizationReport getUtilizationReport() {
		return m_utilizationReport;
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

	public void setUtilizationList(List<com.dianping.cat.home.utilization.entity.Domain> dUList) {
		m_utilizationList = dUList;
	}

	public void setUtilizationReport(UtilizationReport utilizationReport) {
		m_utilizationReport = utilizationReport;
	}

	public void setUtilizationWebList(List<com.dianping.cat.home.utilization.entity.Domain> utilizationWebList) {
		m_utilizationWebList = utilizationWebList;
	}

}
