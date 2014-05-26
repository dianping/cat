package com.dianping.cat.report.task.product;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Files;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.webres.json.JsonArray;
import org.unidal.webres.json.JsonObject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.dal.HostinfoDao;
import com.dianping.cat.core.dal.HostinfoEntity;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;

public class ProductUpdateTask implements Task, LogEnabled {

	@Inject
	private HostinfoDao m_hostInfoDao;

	@Inject
	private ProjectDao m_projectDao;

	private Logger m_logger;

	private static final long DURATION = 60 * 60 * 1000L;

	private static final String CMDB_DOMAIN_URL = "http://cmdb.dp/cmdb/device/s?q=%s&fl=app&tidy=true";

	private static final String CMDB_INFO_URL = "http://cmdb.dp/cmdb/device/s?q=app:%s&fl=rd_duty,project_email&tidy=true";

	private Map<String, String> m_domainToIpMap = new HashMap<String, String>();

	private void buildDomainToIpMap() {
		try {
			List<Hostinfo> infos = m_hostInfoDao.findAllIp(HostinfoEntity.READSET_FULL);

			for (Hostinfo info : infos) {
				m_domainToIpMap.put(info.getDomain(), info.getIp());
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "product_update_task";
	}

	public JsonArray parseDomain(String content) throws Exception {
		JsonObject object = new JsonObject(content);
		JsonArray array = object.getJSONArray("app");

		return array;
	}

	private Map<String, String> parseInfos(String content) throws Exception {
		Map<String, String> infosMap = new HashMap<String, String>();
		JsonObject object = new JsonObject(content);

		JsonArray owners = object.getJSONArray("rd_duty");

		if (owners.length() > 0) {
			infosMap.put("owner", owners.getString(0));
		}

		JsonArray emails = object.getJSONArray("project_email");
		StringBuilder emailBuilder = new StringBuilder(256);
		int length = emails.length();

		if (length > 0) {
			emailBuilder.append(emails.getString(0));

			for (int i = 1; i < length; i++) {
				String tmpEmail = emails.getString(i);
				if (tmpEmail != null && !"".equals(tmpEmail) && !"null".equals(tmpEmail)) {
					emailBuilder.append(",");
					emailBuilder.append(tmpEmail);
				}
			}
		}

		infosMap.put("email", emailBuilder.toString());

		return infosMap;
	}

	private String queryDomainFromCMDB(String ip) {
		try {
			String cmdb = String.format(CMDB_DOMAIN_URL, ip);
			URL url = new URL(cmdb);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			int nRc = conn.getResponseCode();

			if (nRc == HttpURLConnection.HTTP_OK) {
				InputStream input = conn.getInputStream();
				String content = Files.forIO().readFrom(input, "utf-8");
				JsonArray domains = parseDomain(content.trim());
				int length = domains.length();

				if (length == 1) {
					return domains.getString(0);
				} else if (length > 1) {
					m_logger.error("too many domains for ip: " + ip);
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return null;
	}

	private Map<String, String> queryInfosFromCMDB(String cmdbDomain) {
		try {
			String cmdb = String.format(CMDB_INFO_URL, cmdbDomain);
			URL url = new URL(cmdb);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			int nRc = conn.getResponseCode();

			if (nRc == HttpURLConnection.HTTP_OK) {
				InputStream input = conn.getInputStream();
				String content = Files.forIO().readFrom(input, "utf-8");
				return parseInfos(content.trim());
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return null;
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long startMill = System.currentTimeMillis();
			buildDomainToIpMap();

			try {
				List<Project> projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);

				for (Project proj : projects) {
					String cmdbDomain = proj.getCmdbDomain();
					boolean isCmdbDomainChange = false;

					if (cmdbDomain == null || "".equals(cmdbDomain)) {
						String ip = m_domainToIpMap.get(proj.getDomain());
						cmdbDomain = queryDomainFromCMDB(ip);

						if (cmdbDomain == null || "".equals(cmdbDomain)) {
							continue;
						}

						proj.setCmdbDomain(cmdbDomain);
						isCmdbDomainChange = true;
					}

					if (updateProject(proj) || isCmdbDomainChange) {
						m_projectDao.updateByPK(proj, ProjectEntity.UPDATESET_FULL);
					}
				}
			} catch (Throwable e) {
				Cat.logError(e);
			}

			try {
				long executeMills = System.currentTimeMillis() - startMill;

				if (executeMills < DURATION) {
					Thread.sleep(DURATION - executeMills);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	private boolean updateProject(Project proj) {
		Map<String, String> infosMap = queryInfosFromCMDB(proj.getCmdbDomain());
		String owner = infosMap.get("owner");
		String email = infosMap.get("email");
		boolean isProjChanged = false;

		if (!checkIfInvalidOrEqual(owner, proj.getOwner())) {
			proj.setOwner(owner);
			isProjChanged = true;
		}

		if (!checkIfInvalidOrEqual(email, proj.getEmail())) {
			proj.setEmail(email);
			isProjChanged = true;
		}

		return isProjChanged;
	}
	
	private boolean checkIfInvalidOrEqual(String source, String target){
		if(source == null || "".equals(source) || !"null".equals(source)){
			return true;
		}else{
			return source.equals(target);
		}
	}

	@Override
	public void shutdown() {
	}

}
