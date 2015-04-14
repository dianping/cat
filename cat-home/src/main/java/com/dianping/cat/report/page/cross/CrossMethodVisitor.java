package com.dianping.cat.report.page.cross;

import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.transform.BaseVisitor;
import com.dianping.cat.report.page.cross.display.MethodQueryInfo;

public class CrossMethodVisitor extends BaseVisitor {

	private String m_currentRole;

	private String m_remoteIp;

	private String m_method;

	private String m_app;

	private MethodQueryInfo m_info = new MethodQueryInfo();

	public static final String UNKNOWN_PROJECT = "UnknownProject";

	public CrossMethodVisitor(String method) {
		if (method == null) {
			m_method = "";
		} else {
			m_method = method;
		}
	}

	public MethodQueryInfo getInfo() {
		return m_info;
	}

	@Override
	public void visitName(Name name) {
		String methodName = name.getId();
		String ip = m_remoteIp;
		String domain = m_app;

		if (ip.indexOf(":") > -1) {
			ip = ip.substring(0, ip.indexOf(":"));
		}
		if (StringUtils.isEmpty(domain)) {
			domain = UNKNOWN_PROJECT;
		}

		if (methodName.indexOf(m_method) > -1) {
			m_info.add(ip, m_currentRole, domain, methodName, name);
		}
	}

	@Override
	public void visitRemote(Remote remote) {
		m_remoteIp = remote.getId();
		m_currentRole = remote.getRole();
		m_app = remote.getApp();
		super.visitRemote(remote);
	}

}
