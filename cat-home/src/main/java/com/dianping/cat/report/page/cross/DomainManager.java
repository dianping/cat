package com.dianping.cat.report.page.cross;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.webres.helper.Files;
import org.unidal.webres.json.JsonArray;
import org.unidal.webres.json.JsonObject;

import com.dainping.cat.consumer.core.dal.Hostinfo;
import com.dainping.cat.consumer.core.dal.HostinfoDao;
import com.dainping.cat.consumer.core.dal.HostinfoEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;

public class DomainManager implements Initializable, LogEnabled {

	@Inject
	private HostinfoDao m_hostInfoDao;

	@Inject
	private ServerConfigManager m_manager;

	private Map<String, String> m_ipDomains = new ConcurrentHashMap<String, String>();

	private Map<String, String> m_unknownIps = new ConcurrentHashMap<String, String>();

	private Map<String, String> m_cmdbs = new ConcurrentHashMap<String, String>();

	private Logger m_logger;

	private static final String UNKNOWN_IP = "UnknownIp";

	private static final String UNKNOWN_PROJECT = "UnknownProject";

	private static final String CMDB_URL = "http://cmdb.dp/cmdb/device/s?q=%s&fl=app&tidy=true";

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public String getDomainByIp(String ip) {
		String project = m_ipDomains.get(ip);

		if (project == null) {
			project = m_cmdbs.get(ip);

			if (project == null) {
				m_unknownIps.put(ip, ip);
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
				}
			} catch (DalException e) {
				Cat.logError(e);
			}

			Threads.forGroup("Cat").start(new ReloadDomainTask());
		}
	}

	public class ReloadDomainTask implements Task {
		@Override
		public String getName() {
			return "Reload-Ip-DomainInfo";
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
			Set<String> addIps = new HashSet<String>();
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
							addIps.add(ip);
							m_logger.info(String.format("get domain info from cmdb. ip: %s,domain: %s", ip, domain));
						} else {
							m_logger.error(String.format("can't get domain info from cmdb, ip: %s", ip));
						}
					}
				} catch (Exception e) {
					Cat.logError(e);
				}

				for (String temp : addIps) {
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
					queryFromDatabase();
					queryFromCMDB();
				} catch (Throwable e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(10 * 1000);
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
