package com.dianping.cat.report.task.product;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.unidal.helper.Files;
import org.unidal.helper.Threads.Task;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.unidal.webres.json.JsonArray;
import org.unidal.webres.json.JsonObject;

import com.dianping.cat.Cat;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.core.dal.Hostinfo;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.service.ReportService;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.ProjectService;
import com.site.lookup.util.StringUtils;

public class ProjectUpdateTask implements Task, LogEnabled {

	@Inject
	private HostinfoService m_hostInfoService;

	@Inject
	private ProjectService m_projectService;

	@Inject(type = ReportService.class, value = TransactionAnalyzer.ID)
	private ReportService<TransactionReport> m_reportService;

	private Logger m_logger;

	private static final long DURATION = 60 * 60 * 1000L;

	private static final String CMDB_DOMAIN_URL = "http://api.cmdb.dp/api/v0.1/projects/s?private_ip=%s";

	private static final String CMDB_INFO_URL = "http://api.cmdb.dp/api/v0.1/projects/%s";

	private static final String CMDB_BU_URL = "http://api.cmdb.dp/api/v0.1/projects/%s/bu";

	private static final String CMDB_PRODUCT_URL = "http://api.cmdb.dp/api/v0.1/projects/%s/product";

	private static final String CMDB_HOSTNAME_URL = "http://api.cmdb.dp/api/v0.1/ci/s?q=_type:(vserver;server),private_ip:%s&fl=hostname";

	private boolean checkIfNullOrEqual(String source, int target) {
		if (source == null || source.equals("null")) {
			return true;
		} else {
			return Integer.parseInt(source) == target;
		}
	}

	private boolean checkIfNullOrEqual(String source, String target) {
		if (source == null || source.equals("null")) {
			return true;
		} else {
			return source.equals(target);
		}
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

	private String extractStringFromJsonElement(String str) {
		if (str != null && str.startsWith("[\"")) {
			return str.replace("[\"", "").replace("\"]", "");
		} else {
			return str;
		}
	}

	@Override
	public String getName() {
		return "product_update_task";
	}

	private String mergeAndBuildUniqueString(String baseStr, String appendStr) {
		if (StringUtils.isEmpty(appendStr)) {
			return baseStr;
		}

		StringBuilder builder = new StringBuilder(256);
		builder.append(baseStr);

		for (String str : appendStr.split(",")) {
			String tmpStr = extractStringFromJsonElement(str);

			if (builder.indexOf(tmpStr) < 0) {
				builder.append(",");
				builder.append(tmpStr);
			}
		}

		return builder.toString();
	}

	public String parseDomain(String content) throws Exception {
		JsonObject object = new JsonObject(content);
		JsonArray projectArray = object.getJSONArray("projects");

		if (projectArray.length() > 0) {
			JsonObject firstProject = projectArray.getJSONObject(0);
			return firstProject.get("project_name").toString();
		}
		return null;
	}

	public String parseHostname(String content) throws Exception {
		JsonObject object = new JsonObject(content);
		JsonArray resultArray = object.getJSONArray("result");

		if (resultArray.length() > 0) {
			JsonObject firstResult = resultArray.getJSONObject(0);
			return firstResult.get("hostname").toString();
		}
		return null;
	}

	private Map<String, String> parseInfos(String content) throws Exception {
		Map<String, String> infosMap = new HashMap<String, String>();
		JsonObject project = new JsonObject(content).getJSONObject("project");

		if (project == null) {
			return infosMap;
		}

		Object owner = project.get("rd_duty");
		Object email = project.get("project_email");
		Object phone = project.get("rd_mobile");
		Object level = project.get("project_level");

		if (email != null) {
			infosMap.put("owner", owner.toString());
		} else {
			infosMap.put("owner", null);
		}

		if (email != null) {
			infosMap.put("email", email.toString());
		} else {
			infosMap.put("email", null);
		}

		if (phone != null) {
			infosMap.put("phone", phone.toString());
		} else {
			infosMap.put("phone", null);
		}

		if (level != null) {
			infosMap.put("level", level.toString());
		} else {
			infosMap.put("level", null);
		}
		return infosMap;
	}

	private String parseInfo(String content, String jsonName, String attrName) throws Exception {
		JsonObject json = new JsonObject(content).getJSONObject(jsonName);

		if (json != null) {
			Object obj = json.get(attrName);

			if (obj != null) {
				return obj.toString();
			}
		}
		return null;
	}

	private String queryCmdbName(List<String> ips) {
		if (ips != null) {
			Map<String, Integer> nameCountMap = new HashMap<String, Integer>();

			for (String ip : ips) {
				String cmdbDomain = queryDomainFromCMDB(ip);

				if (checkIfValid(cmdbDomain)) {
					Integer count = nameCountMap.get(cmdbDomain);
					if (count == null) {
						nameCountMap.put(cmdbDomain, 1);
					} else {
						nameCountMap.put(cmdbDomain, count + 1);
					}
				}
			}

			String probableDomain = null;
			int maxCount = 0;
			for (Entry<String, Integer> entry : nameCountMap.entrySet()) {
				int currentCount = entry.getValue();

				if (currentCount > maxCount) {
					maxCount = currentCount;
					probableDomain = entry.getKey();
				}
			}

			return probableDomain;
		} else {
			return null;
		}
	}

	private String queryDomainFromCMDB(String ip) {
		Transaction t = Cat.newTransaction("CMDB", "queryDomain");

		try {
			String cmdb = String.format(CMDB_DOMAIN_URL, ip);
			InputStream in = Urls.forIO().readTimeout(1000).connectTimeout(1000).openStream(cmdb);
			String content = Files.forIO().readFrom(in, "utf-8");

			t.setStatus(Transaction.SUCCESS);
			t.addData(content);
			return parseDomain(content.trim());
		} catch (Exception e) {
			Cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
		return null;
	}

	private String queryHostnameFromCMDB(String ip) {
		Transaction t = Cat.newTransaction("CMDB", "queryHostname");
		try {
			String cmdb = String.format(CMDB_HOSTNAME_URL, ip);
			InputStream in = Urls.forIO().readTimeout(1000).connectTimeout(1000).openStream(cmdb);
			String content = Files.forIO().readFrom(in, "utf-8");

			t.setStatus(Transaction.SUCCESS);
			t.addData(content);
			return parseHostname(content.trim());
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
		return null;
	}

	private List<String> queryIpsFromReport(String domain) {
		Date startDate = TimeUtil.getCurrentDay(-2);
		Date endDate = TimeUtil.getCurrentDay();
		TransactionReport report = m_reportService.queryDailyReport(domain, startDate, endDate);
		Set<String> ipSet = report.getMachines().keySet();
		List<String> ipList = new ArrayList<String>();
		ipList.addAll(ipSet);

		return ipList;
	}

	private Map<String, String> queryProjectInfoFromCMDB(String cmdbDomain) {
		Transaction t = Cat.newTransaction("CMDB", "queryProjectInfo");
		try {
			String cmdb = String.format(CMDB_INFO_URL, cmdbDomain);
			InputStream in = Urls.forIO().readTimeout(1000).connectTimeout(1000).openStream(cmdb);
			String content = Files.forIO().readFrom(in, "utf-8");

			t.setStatus(Transaction.SUCCESS);
			t.addData(content);
			return parseInfos(content.trim());
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
		return new HashMap<String, String>();
	}

	private String queryProjectInfoFromCMDB(String url, String jsonName, String attrName) {
		Transaction t = Cat.newTransaction("CMDB", "queryProjectInfo");
		try {
			InputStream in = Urls.forIO().readTimeout(1000).connectTimeout(1000).openStream(url);
			String content = Files.forIO().readFrom(in, "utf-8");

			t.setStatus(Transaction.SUCCESS);
			t.addData(content);
			return parseInfo(content, jsonName, attrName);
		} catch (Exception e) {
			t.setStatus(e);
		} finally {
			t.complete();
		}
		return null;
	}

	@Override
	public void run() {
		boolean active = true;

		while (active) {
			long startMill = System.currentTimeMillis();
			int hour = Calendar.getInstance().get(Calendar.HOUR);
			String hourStr = String.valueOf(hour);

			if (hour < 10) {
				hourStr = "0" + hourStr;
			}

			Transaction t1 = Cat.newTransaction("UpdateHostname", "H" + hourStr);
			try {
				updateHostNameInfo();
				t1.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t1.setStatus(e);
			} finally {
				t1.complete();
			}

			Transaction t2 = Cat.newTransaction("UpdateProjectInfo", "H" + hourStr);
			try {
				updateProjectInfo();
				t2.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t1.setStatus(e);
			} finally {
				t2.complete();
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

	@Override
	public void shutdown() {
	}

	private void updateHostNameInfo() {
		try {
			List<Hostinfo> infos = m_hostInfoService.findAll();

			for (Hostinfo info : infos) {
				try {
					String hostname = info.getHostname();
					String ip = info.getIp();
					String cmdbHostname = queryHostnameFromCMDB(ip);

					if (StringUtils.isEmpty(cmdbHostname)) {
						continue;
					}

					if (StringUtils.isEmpty(hostname) || !hostname.equals(cmdbHostname)) {
						info.setHostname(cmdbHostname);
						m_hostInfoService.updateHostinfo(info);
					} else {
						m_logger.error("can't find hostname for ip: " + ip);
					}
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		} catch (Throwable e) {
			Cat.logError(e);
		}
	}

	private boolean updateProject(Project pro) {
		String cmdbDomain = pro.getCmdbDomain();
		Map<String, String> infosMap = queryProjectInfoFromCMDB(cmdbDomain);
		String cmdbOwner = infosMap.get("owner");
		String cmdbEmail = infosMap.get("email");
		String cmdbPhone = infosMap.get("phone");
		String cmdbLevel = infosMap.get("level");
		String dbOwner = pro.getOwner();
		String dbEmail = pro.getEmail();
		String dbPhone = pro.getPhone();
		int dbLevel = pro.getLevel();
		boolean isProjChanged = false;

		if (!checkIfNullOrEqual(cmdbOwner, dbOwner)) {
			pro.setOwner(mergeAndBuildUniqueString(cmdbOwner, dbOwner));
			isProjChanged = true;
		}
		if (!checkIfNullOrEqual(cmdbEmail, dbEmail)) {
			pro.setEmail(mergeAndBuildUniqueString(cmdbEmail, dbEmail));
			isProjChanged = true;
		}
		if (!checkIfNullOrEqual(cmdbPhone, dbPhone)) {
			pro.setPhone(mergeAndBuildUniqueString(cmdbPhone, dbPhone));
			isProjChanged = true;
		}
		if (!checkIfNullOrEqual(cmdbLevel, dbLevel)) {
			pro.setLevel(Integer.parseInt(cmdbLevel));
			isProjChanged = true;
		}

		String buUrl = String.format(CMDB_BU_URL, cmdbDomain);
		String productlineUrl = String.format(CMDB_PRODUCT_URL, cmdbDomain);
		String cmdbBu = queryProjectInfoFromCMDB(buUrl, "bu", "bu_name");
		String cmdbProductline = queryProjectInfoFromCMDB(productlineUrl, "product", "product_name");
		String dbBu = pro.getBu();
		String dbProductline = pro.getCmdbProductline();

		if (!checkIfNullOrEqual(cmdbBu, dbBu)) {
			pro.setBu(cmdbBu);
			isProjChanged = true;
		}

		if (!checkIfNullOrEqual(cmdbProductline, dbProductline)) {
			pro.setCmdbProductline(cmdbProductline);
			isProjChanged = true;
		}

		return isProjChanged;
	}

	private void updateProjectInfo() {
		try {
			List<Project> projects = m_projectService.findAll();

			for (Project pro : projects) {
				try {
					List<String> ips = queryIpsFromReport(pro.getDomain());
					String originCmdbDomain = pro.getCmdbDomain();
					String cmdbDomain = queryCmdbName(ips);

					if (cmdbDomain != null) {
						boolean isChange = !cmdbDomain.equals(originCmdbDomain);

						pro.setCmdbDomain(cmdbDomain);
						boolean isProjectInfoChange = updateProject(pro);

						if (isProjectInfoChange || isChange) {
							m_projectService.updateProject(pro);
						}
					}
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		} catch (Throwable e) {
			Cat.logError(e);
		}
	}

}
