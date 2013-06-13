package com.dianping.cat.report.page.cross;

import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.entity.Local;
import com.dianping.cat.consumer.cross.model.entity.Name;
import com.dianping.cat.consumer.cross.model.entity.Remote;
import com.dianping.cat.consumer.cross.model.entity.Type;
import com.dianping.cat.consumer.cross.model.transform.BaseVisitor;
import com.dianping.cat.report.page.cross.display.MethodQueryInfo;

public class CrossMethodVisitor extends BaseVisitor {

	private String m_currentRole;

	private String m_remoteIp;

	private String m_method;

	private MethodQueryInfo m_info = new MethodQueryInfo();

	private DomainManager m_manager;

	public CrossMethodVisitor(String method, DomainManager manager) {
		m_method = method;
		m_manager = manager;
	}

	public MethodQueryInfo getInfo() {
		return m_info;
	}

	@Override
	public void visitCrossReport(CrossReport crossReport) {
		super.visitCrossReport(crossReport);
	}

	@Override
	public void visitLocal(Local local) {
		super.visitLocal(local);
	}

	@Override
	public void visitName(Name name) {
		String methodName = name.getId();
		String domain = m_manager.getDomainByIp(m_remoteIp);
		String ip = m_remoteIp;
		
		if(ip.indexOf(":")>-1){
			ip = ip.substring(0,ip.indexOf(":"));
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

	@Override
	public void visitType(Type type) {
		super.visitType(type);
	}

}
