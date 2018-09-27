package com.dianping.cat.report.page.applog.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.app.crash.AppLog;
import com.dianping.cat.app.crash.AppLogContent;
import com.dianping.cat.app.crash.AppLogContentDao;
import com.dianping.cat.app.crash.AppLogContentEntity;
import com.dianping.cat.app.crash.AppLogDao;
import com.dianping.cat.app.crash.AppLogEntity;
import com.dianping.cat.config.LogLevel;
import com.dianping.cat.report.LogMsg;
import com.dianping.cat.report.page.app.service.FieldsInfo;
import com.dianping.cat.report.page.app.service.LogService;
import com.dianping.cat.report.page.applog.display.AppLogDetailInfo;
import com.dianping.cat.report.page.applog.display.AppLogDisplayInfo;

@Named
public class AppLogService extends LogService {

	@Inject
	private AppLogDao m_appLogDao;

	@Inject
	private AppLogContentDao m_appLogContentDao;

	public AppLogDisplayInfo buildAppLogDisplayInfo(AppLogQueryEntity entity) {
		AppLogDisplayInfo info = new AppLogDisplayInfo();

		buildAppLogData(entity, info);
		info.setAppNames(m_mobileConfigManager.queryApps());

		return info;
	}

	private void buildAppLogData(AppLogQueryEntity entity, AppLogDisplayInfo info) {
		Map<String, Set<String>> fieldsMap = new HashMap<String, Set<String>>();
		AppLogFilter appLogFilter = new AppLogFilter(entity.getQuery());

		Date startTime = entity.buildStartTime();
		Date endTime = entity.buildEndTime();
		int appName = entity.getAppName();
		int platform = entity.getPlatform();
		String unionId = entity.getUnionId();
		Map<String, LogMsg> logMsgs = new HashMap<String, LogMsg>();
		int offset = 0;

		try {
			while (true) {
				List<AppLog> result = m_appLogDao.findDataByConditions(startTime, endTime, appName, platform, unionId,
				      offset, LIMIT, AppLogEntity.READSET_FULL);

				for (AppLog log : result) {
					buildFieldsMap(fieldsMap, log);

					if (appLogFilter.checkFlag(log)) {
						buildLogMsg(logMsgs, log.getCategory(), log.getId());
					}
				}

				int count = result.size();
				offset += count;

				if (count < LIMIT) {
					break;
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		info.setMsgs(buildLogMsgList(logMsgs));

		if (!fieldsMap.isEmpty()) {
			info.setFieldsInfo(buildFiledsInfo(fieldsMap));
		}
	}

	private FieldsInfo buildFiledsInfo(Map<String, Set<String>> fieldsMap) {
		Comparator<String> comparator = new Comparator<String>() {
			public int compare(String s1, String s2) {
				return s2.compareTo(s1);
			}
		};

		FieldsInfo fieldsInfo = new FieldsInfo();
		List<String> v = new ArrayList<String>(fieldsMap.get(APP_VERSIONS));
		List<String> p = new ArrayList<String>(fieldsMap.get(PLATFORM_VERSIONS));
		List<String> l = new ArrayList<String>(fieldsMap.get(LEVELS));
		List<String> d = new ArrayList<String>(fieldsMap.get(DEVICES));

		Collections.sort(v, comparator);
		Collections.sort(p, comparator);

		fieldsInfo.setAppVersions(v).setPlatVersions(p).setLevels(l).setDevices(d);

		return fieldsInfo;
	}

	private void buildFieldsMap(Map<String, Set<String>> fieldsMap, AppLog log) {
		findOrCreate(APP_VERSIONS, fieldsMap).add(log.getAppVersion());
		findOrCreate(PLATFORM_VERSIONS, fieldsMap).add(log.getPlatformVersion());
		findOrCreate(LEVELS, fieldsMap).add(LogLevel.getName(log.getLevel()));
		findOrCreate(DEVICES, fieldsMap).add(log.getDeviceBrand() + "-" + log.getDeviceModel());
	}

	public AppLogDetailInfo buildAppLogDetail(int id) {
		AppLogDetailInfo info = new AppLogDetailInfo();

		try {
			AppLog appLog = m_appLogDao.findByPK(id, AppLogEntity.READSET_FULL);

			info.setAppName(m_mobileConfigManager.getAppName(appLog.getAppId()));
			info.setPlatform(m_mobileConfigManager.getPlatformStr(appLog.getPlatform()));
			info.setAppVersion(appLog.getAppVersion());
			info.setPlatformVersion(appLog.getPlatformVersion());
			info.setLevel(LogLevel.getName(appLog.getLevel()));
			info.setDeviceBrand(appLog.getDeviceBrand());
			info.setDeviceModel(appLog.getDeviceModel());
			info.setLogTime(appLog.getLogTime());
			info.setUnionId(appLog.getUnionId());

			AppLogContent detail = m_appLogContentDao.findByPK(id, AppLogContentEntity.READSET_FULL);
			byte[] content = detail.getContent();

			info.setDetail(buildContent(content));
		} catch (Exception e) {
			Cat.logError(e);
		}
		return info;
	}

	public AppLogDisplayInfo buildAppLogGraph(AppLogQueryEntity entity) {
		AppLogDisplayInfo info = new AppLogDisplayInfo();
		AppLogFilter crashLogFilter = new AppLogFilter(entity.getQuery());
		Map<String, Map<String, AtomicInteger>> distributions = new HashMap<String, Map<String, AtomicInteger>>();

		Date startTime = entity.buildStartTime();
		Date endTime = entity.buildEndTime();
		int appName = entity.getAppName();
		int platform = entity.getPlatform();
		String dpid = entity.getUnionId();
		int offset = 0;

		try {
			while (true) {
				List<AppLog> result = m_appLogDao.findDataByConditions(startTime, endTime, appName, platform, dpid, offset,
				      LIMIT, AppLogEntity.READSET_FULL);

				for (AppLog log : result) {
					String category = log.getCategory();
					if (category != null && category.trim().equals(entity.getCategory().trim())
					      && crashLogFilter.checkFlag(log)) {
						buildDistributions(log, distributions);
					}
				}

				int count = result.size();
				offset += count;

				if (count < LIMIT) {
					break;
				}
			}
		} catch (Exception e) {
			Cat.logError(e);
		}

		info.setMsgDistributions(buildDistributionChart(distributions));

		return info;
	}

	private void buildDistributions(AppLog log, Map<String, Map<String, AtomicInteger>> distributions) {
		if (distributions.isEmpty()) {
			Map<String, AtomicInteger> appVersions = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> platVersions = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> devices = new HashMap<String, AtomicInteger>();

			distributions.put(APP_VERSIONS, appVersions);
			distributions.put(PLATFORM_VERSIONS, platVersions);
			distributions.put(DEVICES, devices);
		}

		addCount(log.getAppVersion(), distributions.get(APP_VERSIONS));
		addCount(log.getPlatformVersion(), distributions.get(PLATFORM_VERSIONS));
		addCount(log.getDeviceBrand() + "-" + log.getDeviceModel(), distributions.get(DEVICES));
	}

}
