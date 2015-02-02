package com.dianping.cat.report.page.cross.display;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;
import com.dianping.cat.consumer.cross.model.transform.BaseVisitor;
import com.dianping.cat.service.HostinfoService;

public class ProjectInfo extends BaseVisitor {

	public static final String ALL_SERVER = "AllServers";

	public static final String ALL_CLIENT = "AllClients";

	private static final String UNKNOWN_PROJECT = "UnknownProject";

	private Map<String, TypeDetailInfo> m_callProjectsInfo = new LinkedHashMap<String, TypeDetailInfo>();

	private Map<String, TypeDetailInfo> m_serviceProjectsInfo = new LinkedHashMap<String, TypeDetailInfo>();

	private Map<String, TypeDetailInfo> m_callerProjectsInfo = new LinkedHashMap<String, TypeDetailInfo>();

	private String m_clientIp;

	private long m_reportDuration;

	private String m_callSortBy = "Avg";

	private String m_serviceSortBy = "Avg";

	private HostinfoService m_hostinfoService;

	public ProjectInfo(long reportDuration) {
		m_reportDuration = reportDuration;
	}

	public void addCallerProjectInfo(String domain, TypeDetailInfo info) {
        String allKey = ALL_CLIENT + info.getType();
        TypeDetailInfo all = m_callerProjectsInfo.get(allKey);

		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration, ALL_CLIENT);
			all.setType(info.getType());
			m_callerProjectsInfo.put(allKey, all);
		}
		all.mergeTypeDetailInfo(info);
		m_callerProjectsInfo.put(domain, info);
	}

	private void addCallerProject(String ip, String app, Type type) {
		String projectName = app;

		if (StringUtils.isEmpty(projectName)) {
			projectName = getProjectName(ip);
		}

        String allKey = ALL_CLIENT + type.getId();
        TypeDetailInfo all = m_callerProjectsInfo.get(allKey);
		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration, ALL_CLIENT);
			m_callerProjectsInfo.put(allKey, all);
		}
        String key = projectName + type.getId();
        TypeDetailInfo info = m_callerProjectsInfo.get(key);
		if (info == null) {
			info = new TypeDetailInfo(m_reportDuration, projectName);
			m_callerProjectsInfo.put(key, info);
		}
		info.mergeType(type);
		all.mergeType(type);
	}

	private void addCallProject(String ip, String app, Type type) {
		String projectName = app;

		if (StringUtils.isEmpty(projectName)) {
			projectName = getProjectName(ip);
		}
        String allKey = ALL_SERVER + type.getId();
        TypeDetailInfo all = m_callProjectsInfo.get(allKey);
		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration, ALL_SERVER);
			m_callProjectsInfo.put(allKey, all);
		}
        String key = projectName + type.getId();
        TypeDetailInfo info = m_callProjectsInfo.get(key);
		if (info == null) {
			info = new TypeDetailInfo(m_reportDuration, projectName);
			m_callProjectsInfo.put(key, info);
		}
		info.mergeType(type);
		all.mergeType(type);
	}

	private void addServiceProject(String ip, String app, Type type) {
		String projectName = app;

		if (StringUtils.isEmpty(projectName)) {
			projectName = getProjectName(ip);
		}

        String allKey = ALL_CLIENT + type.getId();
        TypeDetailInfo all = m_serviceProjectsInfo.get(allKey);
		if (all == null) {
			all = new TypeDetailInfo(m_reportDuration, ALL_CLIENT);
			m_serviceProjectsInfo.put(allKey, all);
		}
        String key = projectName + type.getId();
        TypeDetailInfo info = m_serviceProjectsInfo.get(key);
		if (info == null) {
			info = new TypeDetailInfo(m_reportDuration, projectName);
			m_serviceProjectsInfo.put(key, info);
		}
		info.mergeType(type);
		all.mergeType(type);
	}

	public Map<String, TypeDetailInfo> getAllCallProjectInfo() {
		return m_callProjectsInfo;
	}

	public Collection<TypeDetailInfo> getCallProjectsInfo() {
		List<TypeDetailInfo> values = new ArrayList<TypeDetailInfo>(m_callProjectsInfo.values());
		Collections.sort(values, new TypeComparator(m_callSortBy));
		return values;
	}

	public Map<String, TypeDetailInfo> getCallerProjectsInfo() {
		return m_callerProjectsInfo;
	}

	public List<TypeDetailInfo> getServiceProjectsInfo() {
		List<TypeDetailInfo> values = new ArrayList<TypeDetailInfo>(m_serviceProjectsInfo.values());
		Collections.sort(values, new TypeComparator(m_serviceSortBy));
		return values;
	}

	public String getProjectName(String ip) {
		if (StringUtils.isEmpty(ip)) {
			return UNKNOWN_PROJECT;
		}
		if (ip.indexOf(':') > 0) {
			ip = ip.substring(0, ip.indexOf(':'));
		}
		return m_hostinfoService.queryDomainByIp(ip);
	}

	public long getReportDuration() {
		return m_reportDuration;
	}

	public ProjectInfo setCallSortBy(String callSoryBy) {
		m_callSortBy = callSoryBy;
		return this;
	}

	public ProjectInfo setClientIp(String clientIp) {
		m_clientIp = clientIp;
		return this;
	}

	public void setHostinfoService(HostinfoService hostinfoService) {
		m_hostinfoService = hostinfoService;
	}

	public ProjectInfo setServiceSortBy(String serviceSortBy) {
		m_serviceSortBy = serviceSortBy;
		return this;
	}

	@Override
	public void visitCrossReport(CrossReport crossReport) {
		super.visitCrossReport(crossReport);
	}

	@Override
	public void visitLocal(Local local) {
		if (m_clientIp.equals("All") || m_clientIp.equals(local.getId())) {
			super.visitLocal(local);
		}
	}

	@Override
	public void visitRemote(Remote remote) {
		String remoteIp = remote.getId();
		String role = remote.getRole();
		String app = remote.getApp();

		if (role != null && role.endsWith("Client")) {
			addServiceProject(remoteIp, app, remote.getType());
		} else if (role != null && role.endsWith("Server")) {
			addCallProject(remoteIp, app, remote.getType());
		} else if (role != null && role.endsWith("Caller") && remoteIp.endsWith(":Caller")) {
			addCallerProject(remoteIp, app, remote.getType());
		}
	}

}
