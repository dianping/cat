package com.dianping.cat;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Files;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.webres.json.JsonArray;
import org.unidal.webres.json.JsonObject;

import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.dal.HostinfoDao;
import com.dianping.cat.core.dal.HostinfoEntity;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;

public class DomainManager implements Initializable, LogEnabled {

	@Inject
	private HostinfoDao m_hostInfoDao;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private ServerConfigManager m_manager;

	private Map<String, String> m_ipDomains = new ConcurrentHashMap<String, String>();

	private Map<String, String> m_unknownIps = new ConcurrentHashMap<String, String>();

	private Map<String, String> m_cmdbs = new ConcurrentHashMap<String, String>();

	private Set<String> m_domainsInCat = new HashSet<String>();

	private Map<String, Hostinfo> m_ipsInCat = new ConcurrentHashMap<String, Hostinfo>();

	private Logger m_logger;

	private static final String UNKNOWN_IP = "UnknownIp";

	private static final String UNKNOWN_PROJECT = "UnknownProject";

	private static final String CMDB_URL = "http://cmdb.dp/cmdb/device/s?q=%s&fl=app&tidy=true";

	public boolean containsDomainInCat(String domain) {
		return m_domainsInCat.contains(domain);
	}

	public Hostinfo queryHostInfoByIp(String ip) {
		return m_ipsInCat.get(ip);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public boolean insert(String domain, String ip) {
		try {
			Hostinfo info = m_hostInfoDao.createLocal();

			info.setDomain(domain);
			info.setIp(ip);
			m_hostInfoDao.insert(info);
			m_domainsInCat.add(domain);
			m_ipsInCat.put(ip, info);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

	public String queryDomainByIp(String ip) {
		String project = m_ipDomains.get(ip);

		if (project == null) {
			project = m_cmdbs.get(ip);

			if (project == null) {
				if (!m_unknownIps.containsKey(ip)) {
					m_unknownIps.put(ip, ip);
				}
				return UNKNOWN_PROJECT;
			}
		}
		return project;
	}

	@Override
	public void initialize() throws InitializationException {
		if (!m_manager.isLocalMode()) {
			try {
				m_ipDomains.put(UNKNOWN_IP, UNKNOWN_PROJECT);
				List<Hostinfo> infos = m_hostInfoDao.findAllIp(HostinfoEntity.READSET_FULL);
				for (Hostinfo info : infos) {
					m_ipDomains.put(info.getIp(), info.getDomain());
					m_domainsInCat.add(info.getDomain());
					m_ipsInCat.put(info.getIp(), info);
				}

				List<Project> projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);
				for (Project project : projects) {
					m_domainsInCat.add(project.getDomain());
				}
			} catch (DalException e) {
				Cat.logError(e);
			}
			Threads.forGroup("Cat").start(new ReloadDomainTask());
		}
	}

	public class ReloadDomainTask implements Task {

		private int m_count;

		@Override
		public String getName() {
			return "Reload-CMDB-Ip-Domain-Info";
		}

		public String parseIp(String content) throws Exception {
			JsonObject object = new JsonObject(content);
			JsonArray array = object.getJSONArray("app");

			if (array.length() > 0) {
				return array.getString(0);
			}
			return null;
		}

		private void queryFromCMDB() {
			Set<String> addedIps = new HashSet<String>();
			for (String ip : m_unknownIps.keySet()) {
				try {
					String cmdb = String.format(CMDB_URL, ip);
					URL url = new URL(cmdb);
					HttpURLConnection conn = (HttpURLConnection) url.openConnection();
					int nRc = conn.getResponseCode();

					if (nRc == HttpURLConnection.HTTP_OK) {
						InputStream input = conn.getInputStream();
						String content = Files.forIO().readFrom(input, "utf-8");
						String domain = parseIp(content.trim());

						if (domain != null) {
							m_cmdbs.put(ip, domain);
							addedIps.add(ip);
						}
					}
				} catch (Exception e) {
					Cat.logError(e);
				}

				for (String temp : addedIps) {
					m_unknownIps.remove(temp);
				}
			}
		}

		private void queryFromDatabase() {
			Set<String> addIps = new HashSet<String>();
			for (String ip : m_unknownIps.keySet()) {
				try {
					Hostinfo hostinfo = m_hostInfoDao.findByIp(ip, HostinfoEntity.READSET_FULL);

					addIps.add(hostinfo.getIp());
					m_ipDomains.put(hostinfo.getIp(), hostinfo.getDomain());
					m_domainsInCat.add(hostinfo.getDomain());
				} catch (Exception e) {
					// ignore
				}
			}
			for (String ip : addIps) {
				m_unknownIps.remove(ip);
			}
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					m_count++;
					queryFromDatabase();
					queryFromCMDB();
					if (m_count % 1000 == 0 && m_unknownIps.size() > 0) {
						m_logger.error(String.format("can't get domain info from cmdb, ip: %s", m_unknownIps.keySet()
						      .toString()));
					}
				} catch (Throwable e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(2 * 60 * 1000);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

	public boolean insertDomain(String domain) {
		Project project = m_projectDao.createLocal();

		project.setDomain(domain);
		project.setProjectLine("Default");
		project.setDepartment("Default");
		try {
			m_projectDao.insert(project);
			m_domainsInCat.add(domain);

			return true;
		} catch (Exception ex) {
			Cat.logError(ex);
		}
		return false;
	}

	public boolean update(int id, String domain, String ip) {
		try {
			Hostinfo info = m_hostInfoDao.createLocal();

			info.setId(id);
			info.setDomain(domain);
			info.setIp(ip);
			info.setLastModifiedDate(new Date());
			m_hostInfoDao.updateByPK(info, HostinfoEntity.UPDATESET_FULL);
			m_domainsInCat.add(domain);
			m_ipsInCat.put(ip, info);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return false;
	}

}
