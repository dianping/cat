package com.dianping.cat.report.page.cross;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.unidal.web.mvc.view.annotation.EntityMeta;
import org.unidal.web.mvc.view.annotation.ModelMeta;

import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.helper.SortHelper;
import com.dianping.cat.mvc.AbstractReportModel;
import com.dianping.cat.report.ReportPage;
import com.dianping.cat.report.page.cross.display.HostInfo;
import com.dianping.cat.report.page.cross.display.MethodInfo;
import com.dianping.cat.report.page.cross.display.MethodQueryInfo;
import com.dianping.cat.report.page.cross.display.ProjectInfo;

@ModelMeta(CrossAnalyzer.ID)
public class Model extends AbstractReportModel<Action, ReportPage, Context> {

	private String m_callSort;

	private String m_queryName;

	@EntityMeta
	private HostInfo m_hostInfo;

	@EntityMeta
	private MethodInfo m_methodInfo;

	@EntityMeta
	private ProjectInfo m_projectInfo;

	@EntityMeta
	private CrossReport m_report;

	private String m_serviceSort;

	@EntityMeta
	private MethodQueryInfo m_info;

	public Model(Context ctx) {
		super(ctx);
	}

	public String getCallSort() {
		return m_callSort;
	}

	@Override
	public Action getDefaultAction() {
		return Action.HOURLY_PROJECT;
	}

	@Override
	public String getDomain() {
		if (m_report == null) {
			return getDisplayDomain();
		} else {
			return m_report.getDomain();
		}
	}

	@Override
	public Collection<String> getDomains() {
		if (m_report == null) {
			ArrayList<String> arrayList = new ArrayList<String>();

			arrayList.add(getDomain());
			return arrayList;
		} else {
			Set<String> domainNames = m_report.getDomainNames();

			return SortHelper.sortDomain(domainNames);
		}
	}

	public HostInfo getHostInfo() {
		return m_hostInfo;
	}

	public MethodQueryInfo getInfo() {
		return m_info;
	}

	public List<String> getIps() {
		if (m_report == null) {
			return new ArrayList<String>();
		} else {
			return SortHelper.sortIpAddress(m_report.getIps());
		}
	}

	public MethodInfo getMethodInfo() {
		return m_methodInfo;
	}

	public ProjectInfo getProjectInfo() {
		return m_projectInfo;
	}

	public String getQueryName() {
		return m_queryName;
	}

	public CrossReport getReport() {
		return m_report;
	}

	public String getServiceSort() {
		return m_serviceSort;
	}

	public void setCallSort(String callSort) {
		m_callSort = callSort;
	}

	public void setHostInfo(HostInfo hostInfo) {
		m_hostInfo = hostInfo;
	}

	public void setInfo(MethodQueryInfo info) {
		m_info = info;
	}

	public void setMethodInfo(MethodInfo methodInfo) {
		m_methodInfo = methodInfo;
	}

	public void setProjectInfo(ProjectInfo projectInfo) {
		m_projectInfo = projectInfo;
	}

	public void setQueryName(String queryName) {
		m_queryName = queryName;
	}

	public void setReport(CrossReport report) {
		m_report = report;
	}

	public void setServiceSort(String serviceSort) {
		m_serviceSort = serviceSort;
	}

}
