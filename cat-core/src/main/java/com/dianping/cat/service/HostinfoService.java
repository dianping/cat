package com.dianping.cat.service;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

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

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.dal.HostinfoDao;
import com.dianping.cat.core.dal.HostinfoEntity;
import com.site.lookup.util.StringUtils;

public class HostinfoService implements Initializable, LogEnabled {

	@Inject
	private HostinfoDao m_hostinfoDao;

	@Inject
	private ServerConfigManager m_manager;

	private Map<String, String> m_ipDomains = new ConcurrentHashMap<String, String>();

	private Map<String, String> m_unknownIps = new ConcurrentHashMap<String, String>();

	private Map<String, String> m_cmdbs = new ConcurrentHashMap<String, String>();

	private Map<String, Hostinfo> m_hostinfos = new ConcurrentHashMap<String, Hostinfo>();

	private static final String UNKNOWN_IP = "UnknownIp";

	private static final String UNKNOWN_PROJECT = "UnknownProject";

	private static final String CMDB_URL = "http://cmdb.dp/cmdb/device/s?q=%s&fl=app&tidy=true";

	protected Logger m_logger;

	public Hostinfo createLocal() {
		return m_hostinfoDao.createLocal();
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public List<Hostinfo> findAll() throws DalException {
		return new ArrayList<Hostinfo>(m_hostinfos.values());
	}

	public Hostinfo findByIp(String ip) {
		Hostinfo hostinfo = m_hostinfos.get(ip);

		if (hostinfo != null) {
			return hostinfo;
		} else {
			try {
				return m_hostinfoDao.findByIp(ip, HostinfoEntity.READSET_FULL);
			} catch (DalException e) {
				return new Hostinfo();
			}
		}
	}

	@Override
	public void initialize() throws InitializationException {
		if (!m_manager.isLocalMode()) {
			m_ipDomains.put(UNKNOWN_IP, UNKNOWN_PROJECT);

			Threads.forGroup("Cat").start(new ReloadDomainTask());
		}
	}

	private boolean insert(Hostinfo hostinfo) throws DalException {
		m_hostinfos.put(hostinfo.getIp(), hostinfo);

		int result = m_hostinfoDao.insert(hostinfo);
		if (result == 1) {
			return true;
		} else {
			return false;
		}
	}

	public boolean insert(String domain, String ip) {
		try {
			Hostinfo info = createLocal();

			info.setDomain(domain);
			info.setIp(ip);
			insert(info);
			m_hostinfos.put(ip, info);
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

	public String queryHostnameByIp(String ip) {
		try {
			if (validateIp(ip)) {
				Hostinfo info = m_hostinfos.get(ip);
				String hostname = null;

				if (info != null) {
					hostname = info.getHostname();

					if (StringUtils.isNotEmpty(hostname)) {
						return hostname;
					}
				}
				info = findByIp(ip);

				if (info != null) {
					m_hostinfos.put(ip, info);
					hostname = info.getHostname();
				}
				return hostname;
			} else {
				return null;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return null;
	}

	private void refresh() {
		try {
			List<Hostinfo> hostinfos = m_hostinfoDao.findAllIp(HostinfoEntity.READSET_FULL);

			for (Hostinfo hostinfo : hostinfos) {
				m_hostinfos.put(hostinfo.getIp(), hostinfo);
				m_ipDomains.put(hostinfo.getIp(), hostinfo.getDomain());
			}
		} catch (DalException e) {
			Cat.logError("initialize HostService error", e);
		}
	}

	public boolean update(int id, String domain, String ip) {
		Hostinfo info = createLocal();

		info.setId(id);
		info.setDomain(domain);
		info.setIp(ip);
		info.setLastModifiedDate(new Date());
		updateHostinfo(info);
		m_hostinfos.put(ip, info);
		return true;
	}

	public boolean updateHostinfo(Hostinfo hostinfo) {
		m_hostinfos.put(hostinfo.getIp(), hostinfo);

		try {
			m_hostinfoDao.updateByPK(hostinfo, HostinfoEntity.UPDATESET_FULL);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	private boolean validateIp(String str) {
		Pattern pattern = Pattern
		      .compile("^((\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])\\.){3}(\\d|[1-9]\\d|1\\d\\d|2[0-4]\\d|25[0-5]|[*])$");
		return pattern.matcher(str).matches();
	}

	public class ReloadDomainTask implements Task {

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
					Hostinfo hostinfo = findByIp(ip);

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

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					refresh();
					queryFromDatabase();
					queryFromCMDB();
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

}
