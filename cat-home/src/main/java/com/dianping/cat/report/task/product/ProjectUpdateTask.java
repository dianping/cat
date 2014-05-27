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
import com.site.lookup.util.StringUtils;

public class ProjectUpdateTask implements Task, LogEnabled {

	@Inject
	private HostinfoDao m_hostInfoDao;

	@Inject
	private ProjectDao m_projectDao;

	private Logger m_logger;

	private Map<String, String> m_domainToIpMap = new HashMap<String, String>();

	private static final long DURATION = 60 * 60 * 1000L;

	private static final String CMDB_DOMAIN_URL = "http://cmdb.dp/cmdb/device/s?q=%s&fl=app&tidy=true";

	private static final String CMDB_INFO_URL = "http://cmdb.dp/cmdb/device/s?q=app:%s&fl=rd_duty,project_email,project_owner_mobile&tidy=true";

	private static final String CMDB_HOSTNAME_URL = "http://cmdb.dp/cmdb/device/s?q=%s&fl=hostname&tidy=true";

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

	private String buildStringFromJsonArray(JsonArray array) {
		int length = array.length();

		if (length > 0) {
			StringBuilder builder = new StringBuilder(256);

			for (int i = 0; i < length; i++) {
				String tmpString = array.getString(i);

				if (checkIfValid(tmpString) && builder.indexOf(tmpString) < 0) {
					builder.append(tmpString);
					builder.append(",");
				}
			}

			int builderLength = builder.length();

			if (builderLength > 0) {
				String result = builder.substring(0, builderLength - 1);

				return result;
			}
		}

		return null;
	}

	private boolean checkIfEqual(String source, String target) {
		return source.equals(target);
	}

	private boolean checkIfValid(String source) {
		if (source == null || "".equals(source) || "null".equals(source)) {
			return false;
		}
		return true;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getName() {
		return "product_update_task";
	}

	public String parseDomain(String content) throws Exception {
		JsonObject object = new JsonObject(content);
		JsonArray array = object.getJSONArray("app");

		if (array.length() > 0) {
			return array.getString(0);
		}

		return null;
	}

	public String parseHostname(String content) throws Exception {
		JsonObject object = new JsonObject(content);
		JsonArray array = object.getJSONArray("hostname");

		if (array.length() > 0) {
			return array.getString(0);
		}
		return null;
	}

	private Map<String, String> parseInfos(String content) throws Exception {
		Map<String, String> infosMap = new HashMap<String, String>();
		JsonObject object = new JsonObject(content);
		JsonArray owners = object.getJSONArray("rd_duty");

		if (owners.length() > 0) {
			infosMap.put("owner", owners.getString(0));
		}

		JsonArray emails = object.getJSONArray("project_email");
		String email = buildStringFromJsonArray(emails);
		if (email != null) {
			infosMap.put("email", email);
		}

		JsonArray phones = object.getJSONArray("project_owner_mobile");
		String phone = buildStringFromJsonArray(phones);
		if (phone != null) {
			infosMap.put("phone", phone);
		}

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
				String domain = parseDomain(content.trim());

				if (checkIfValid(domain)) {
					return domain;
				}

				m_logger.error("cannt find single domain for ip: " + ip);
				return null;
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		return null;
	}

	private String queryHostnameFromCMDB(String ip) {
		try {
			String cmdb = String.format(CMDB_HOSTNAME_URL, ip);
			URL url = new URL(cmdb);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			int nRc = conn.getResponseCode();

			if (nRc == HttpURLConnection.HTTP_OK) {
				InputStream input = conn.getInputStream();
				String content = Files.forIO().readFrom(input, "utf-8");
				return parseHostname(content.trim());
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
			updateProjectInfo();
			updateHostNameInfo();

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

	@Override
	public void shutdown() {
	}

	private void updateHostNameInfo() {
		try {
			List<Hostinfo> infos = m_hostInfoDao.findAllIp(HostinfoEntity.READSET_FULL);

			for (Hostinfo info : infos) {
				String hostname = info.getHostname();
				String ip = info.getIp();
				String cmdbHostname = queryHostnameFromCMDB(ip);

				if (StringUtils.isEmpty(hostname) || !hostname.equals(cmdbHostname)) {
					info.setHostname(cmdbHostname);
					m_hostInfoDao.updateByPK(info, HostinfoEntity.UPDATESET_FULL);
				} else {
					m_logger.error("cant find hostname for ip: " + ip);

				}
			}
		} catch (Throwable e) {
			Cat.logError(e);
		}
	}

	private boolean updateProject(Project proj) {
		Map<String, String> infosMap = queryInfosFromCMDB(proj.getCmdbDomain());
		String owner = infosMap.get("owner");
		String email = infosMap.get("email");
		String phone = infosMap.get("phone");
		String dbOwner = proj.getOwner();
		String dbEmail = proj.getEmail();
		String dbPhone = proj.getPhone();
		boolean isProjChanged = false;

		if (checkIfValid(owner) && !checkIfEqual(owner, dbOwner)) {
			proj.setOwner(owner);
			isProjChanged = true;
		}

		if (checkIfValid(email) && !checkIfEqual(email, dbEmail)) {
			proj.setEmail(email);
			isProjChanged = true;
		}

		if (checkIfValid(phone) && !checkIfEqual(phone, dbPhone)) {
			proj.setPhone(phone);
			isProjChanged = true;
		}

		return isProjChanged;
	}

	private void updateProjectInfo() {
		buildDomainToIpMap();

		try {
			List<Project> projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);

			for (Project proj : projects) {
				String cmdbDomain = proj.getCmdbDomain();
				boolean isCmdbDomainChange = false;

				if (!checkIfValid(cmdbDomain)) {
					String ip = m_domainToIpMap.get(proj.getDomain());
					cmdbDomain = queryDomainFromCMDB(ip);

					if (!checkIfValid(cmdbDomain)) {
						continue;
					}

					proj.setCmdbDomain(cmdbDomain);
					isCmdbDomainChange = true;
				}

				boolean isProjectInfoChange = updateProject(proj);

				if (isProjectInfoChange || isCmdbDomainChange) {
					m_projectDao.updateByPK(proj, ProjectEntity.UPDATESET_FULL);
				}
			}
		} catch (Throwable e) {
			Cat.logError(e);
		}
	}

}
