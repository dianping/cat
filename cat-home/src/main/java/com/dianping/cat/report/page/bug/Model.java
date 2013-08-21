package com.dianping.cat.report.page.bug;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import com.dianping.cat.helper.CatString;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.bug.transform.DefaultJsonBuilder;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.report.page.AbstractReportModel;
import com.dianping.cat.report.page.bug.Handler.ErrorStatis;

public class Model extends AbstractReportModel<Action, Context> {

	private BugReport m_bugReport;

	private ServiceReport m_serviceReport;

	private List<com.dianping.cat.home.service.entity.Domain> serviceList;

	public ServiceReport getServiceReport() {
		return m_serviceReport;
	}

	public void setServiceReport(ServiceReport serviceReport) {
		m_serviceReport = serviceReport;
	}

	public List<com.dianping.cat.home.service.entity.Domain> getServiceList() {
		return serviceList;
	}

	public void setServiceList(List<com.dianping.cat.home.service.entity.Domain> serviceList) {
		this.serviceList = serviceList;
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
		return Action.HOURLY_REPORT;
	}

	@Override
	public String getDomain() {
		return CatString.CAT;
	}

	@Override
	public Collection<String> getDomains() {
		return new ArrayList<String>();
	}
}
