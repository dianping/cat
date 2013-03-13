package com.dianping.cat.report.page.cross;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dainping.cat.consumer.dal.report.Hostinfo;
import com.dainping.cat.consumer.dal.report.HostinfoDao;
import com.dainping.cat.consumer.dal.report.HostinfoEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;

public class DomainManager implements Initializable {

	@Inject
	private HostinfoDao m_hostInfoDao;

	@Inject
	private ServerConfigManager m_manager;

	private Map<String, String> m_ipDomains = new HashMap<String, String>();

	private Set<String> m_unknownIps = new HashSet<String>();

	private static final String UNKNOWN_PROJECT = "UnknownProject";

	public String getDomainByIp(String ip) {
		String project = m_ipDomains.get(ip);

		if (project == null) {
			m_unknownIps.add(ip);
			return UNKNOWN_PROJECT;
		}
		return project;
	}

	@Override
	public void initialize() throws InitializationException {
		if (!m_manager.isLocalMode()) {
			try {
				List<Hostinfo> infos = m_hostInfoDao.findAllIp(HostinfoEntity.READSET_FULL);
				for (Hostinfo info : infos) {
					m_ipDomains.put(info.getIp(), info.getDomain());
				}
			} catch (DalException e) {
				Cat.logError(e);
			}

			Threads.forGroup("Cat").start(new ReloadDomainTask());
		}
	}

	class ReloadDomainTask implements Task {
		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					Set<String> addIps = new HashSet<String>();
					synchronized (m_unknownIps) {

						for (String ip : m_unknownIps) {
							try {
								Hostinfo hostinfo = m_hostInfoDao.findByIp(ip, HostinfoEntity.READSET_FULL);
								addIps.add(hostinfo.getIp());
								m_ipDomains.put(hostinfo.getIp(), hostinfo.getDomain());
							} catch (Exception e) {
								// ignore
							}
						}
						for (String ip : addIps) {
							m_unknownIps.remove(ip);
						}

					}
				} catch (Exception e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(60 * 60 * 1000);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public String getName() {
			return "Reload-Ip-DomainInfo";
		}

		@Override
		public void shutdown() {
		}
	}
}
