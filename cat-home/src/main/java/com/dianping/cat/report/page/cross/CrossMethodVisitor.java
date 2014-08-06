package com.dianping.cat.report.page.cross;

import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.transform.BaseVisitor;
import com.dianping.cat.report.page.cross.display.MethodQueryInfo;
import com.dianping.cat.service.HostinfoService;

public class CrossMethodVisitor extends BaseVisitor {

	private String m_currentRole;

	private String m_remoteIp;

	private String m_method;

	private MethodQueryInfo m_info = new MethodQueryInfo();

	private HostinfoService m_hostinfoService;

	public CrossMethodVisitor(String method, HostinfoService hostinfoService) {
		if (method == null) {
			m_method = "";
		} else {
			m_method = method;
		}
		m_hostinfoService = hostinfoService;
	}

	public MethodQueryInfo getInfo() {
		return m_info;
	}

	@Override
	public void visitName(Name name) {
		String methodName = name.getId();
		String domain = m_hostinfoService.queryDomainByIp(m_remoteIp);
		String ip = m_remoteIp;

		if (ip.indexOf(":") > -1) {
			ip = ip.substring(0, ip.indexOf(":"));
		}

		if (methodName.indexOf(m_method) > -1) {
			m_info.add(ip, m_currentRole, domain, methodName, name);
		}
	}

	@Override
	public void visitRemote(Remote remote) {
		m_remoteIp = remote.getId();
		m_currentRole = remote.getRole();
		super.visitRemote(remote);
	}

}
