package com.dianping.cat.report.page.bug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.transform.DefaultJsonBuilder;
import com.dianping.cat.home.heavy.entity.HeavyReport;
import com.dianping.cat.home.heavy.entity.Service;
import com.dianping.cat.home.heavy.entity.Url;
import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;
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

	public ServiceReport getServiceReport() {
		return m_serviceReport;
	}

	public void setServiceReport(ServiceReport serviceReport) {
		m_serviceReport = serviceReport;
	}

	public List<com.dianping.cat.home.service.entity.Domain> getServiceList() {
		return m_serviceList;
	}

	public void setServiceList(List<com.dianping.cat.home.service.entity.Domain> serviceList) {
		this.m_serviceList = serviceList;
	}

	private Map<String, ErrorStatis> m_errorStatis;

	public BugReport getBugReport() {
		return m_bugReport;
	}

	public void setBugReport(BugReport bugReport) {
		m_bugReport = bugReport;
	}

	public Map<String, ErrorStatis> getErrorStatis() {
		return m_errorStatis;
	}

	public void setErrorStatis(Map<String, ErrorStatis> errorStatis) {
		m_errorStatis = errorStatis;
	}

	public Model(Context ctx) {
		super(ctx);
	}

	public String getBugs() {
		return new DefaultJsonBuilder().buildJson(m_bugReport);
	}

	@Override
	public Action getDefaultAction() {
		return Action.BUG_REPORT;
	}

	@Override
	public String getDomain() {
		return CatString.CAT;
	}

	public HeavyReport getHeavyReport() {
		return m_heavyReport;
	}

	public void setHeavyReport(HeavyReport heavyReport) {
		m_heavyReport = heavyReport;
	}

	public List<Url> getCallUrls() {
		return m_callUrls;
	}

	public void setCallUrls(List<Url> callUrls) {
		m_callUrls = callUrls;
	}

	public List<Service> getCallServices() {
		return m_callServices;
	}

	public void setCallServices(List<Service> callServices) {
		m_callServices = callServices;
	}

	public List<Url> getSqlUrls() {
		return m_sqlUrls;
	}

	public void setSqlUrls(List<Url> sqlUrls) {
		m_sqlUrls = sqlUrls;
	}

	public List<Service> getSqlServices() {
		return m_sqlServices;
	}

	public void setSqlServices(List<Service> sqlServices) {
		m_sqlServices = sqlServices;
	}

	public List<Url> getCacheUrls() {
		return m_cacheUrls;
	}

	public void setCacheUrls(List<Url> cacheUrls) {
		m_cacheUrls = cacheUrls;
	}

	public List<Service> getCacheServices() {
		return m_cacheServices;
	}

	public void setCacheServices(List<Service> cacheServices) {
		m_cacheServices = cacheServices;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}
}
