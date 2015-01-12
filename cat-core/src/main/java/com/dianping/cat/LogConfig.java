package com.dianping.cat;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.Transaction;

public class LogConfig implements Initializable {

	private Set<String> m_unusedTypes = new HashSet<String>();

	private Set<String> m_unusedNames = new HashSet<String>();
	
	public boolean isClient(String type) {
		return "PigeonCall".equals(type) || "Call".equals(type);
	}

	public boolean isServer(String type) {
		return "PigeonService".equals(type) || "Service".equals(type);
	}

	public boolean discardTransaction(Transaction t) {
		String type = t.getType();
		String name = t.getName();

		if (m_unusedTypes.contains(type) && m_unusedNames.contains(name)) {
			return true;
		}
		return false;
	}

	@Override
	public void initialize() throws InitializationException {
		m_unusedTypes.add("Service");
		m_unusedTypes.add("PigeonService");
		m_unusedTypes.add("URL");
		m_unusedNames.add("piegonService:heartTaskService:heartBeat");
		m_unusedNames.add("piegonService:heartTaskService:heartBeat()");
		m_unusedNames.add("pigeon:HeartBeatService:null");
		m_unusedNames.add("");
		m_unusedNames.add("/");
		m_unusedNames.add("/index.jsp");
		m_unusedNames.add("/Heartbeat.html");
		m_unusedNames.add("/heartbeat.html");
		m_unusedNames.add("/heartbeat.jsp");
		m_unusedNames.add("/inspect/healthcheck");
		m_unusedNames.add("/MonitorServlet");
		m_unusedNames.add("/monitorServlet?client=f5");
	}

}
