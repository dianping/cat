package com.dianping.cat.report.page.crash.service;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Files;
import org.unidal.helper.Urls;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.app.crash.CrashLog;
import com.dianping.cat.app.crash.CrashLogContent;
import com.dianping.cat.app.crash.CrashLogContentDao;
import com.dianping.cat.app.crash.CrashLogContentEntity;
import com.dianping.cat.app.crash.CrashLogDao;
import com.dianping.cat.app.crash.CrashLogEntity;
import com.dianping.cat.config.Level;
import com.dianping.cat.config.app.CrashLogConfigManager;
import com.dianping.cat.helper.Status;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.LogMsg;
import com.dianping.cat.report.graph.LineChart;
import com.dianping.cat.report.page.app.service.FieldsInfo;
import com.dianping.cat.report.page.app.service.LogService;
import com.dianping.cat.report.page.crash.display.CrashLogDetailInfo;
import com.dianping.cat.report.page.crash.display.CrashLogDisplayInfo;

@Named
public class CrashLogService extends LogService {

	@Inject
	private CrashLogContentDao m_crashLogContentDao;

	@Inject
	private CrashLogDao m_crashLogDao;

	@Inject
	private CrashLogConfigManager m_crashLogConfig;

	private String MODULES = "modules";

	private static final String MAPPER = "mapper";

	public CrashLogDisplayInfo buildCrashGraph(CrashLogQueryEntity entity) {
		CrashLogDisplayInfo info = new CrashLogDisplayInfo();
		buildCrashGraph(entity, info);

		return info;
	}

	private void buildCrashGraph(CrashLogQueryEntity entity, CrashLogDisplayInfo info) {
		CrashLogFilter crashLogFilter = new CrashLogFilter(entity.getQuery());
		Map<String, Map<String, AtomicInteger>> distributions = new HashMap<String, Map<String, AtomicInteger>>();

		Date startTime = entity.buildStartTime();
		Date endTime = entity.buildEndTime();
		String appName = entity.getAppName();
		int platform = entity.getPlatform();
		String dpid = entity.getDpid();
		int offset = 0;

		try {
			while (true) {
				List<CrashLog> result = m_crashLogDao.findDataByConditions(startTime, endTime, appName, platform, dpid,
				      offset, LIMIT, CrashLogEntity.READSET_FULL);

				for (CrashLog log : result) {
					if (log.getMsg() != null && log.getMsg().trim().equals(entity.getMsg().trim())
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

	}

	private void buildCrashLogData(CrashLogQueryEntity entity, CrashLogDisplayInfo info) {
		Map<String, Set<String>> fieldsMap = new HashMap<String, Set<String>>();
		CrashLogFilter crashLogFilter = new CrashLogFilter(entity.getQuery());
		Map<String, Map<String, AtomicInteger>> distributions = new HashMap<String, Map<String, AtomicInteger>>();

		Date startTime = entity.buildStartTime();
		Date endTime = entity.buildEndTime();
		String appName = entity.getAppName();
		int platform = entity.getPlatform();
		String dpid = entity.getDpid();
		Map<String, LogMsg> errorMsgs = new HashMap<String, LogMsg>();
		int offset = 0;
		int totalCount = 0;

		try {
			while (true) {
				List<CrashLog> result = m_crashLogDao.findDataByConditions(startTime, endTime, appName, platform, dpid,
				      offset, LIMIT, CrashLogEntity.READSET_FULL);

				for (CrashLog log : result) {
					buildFieldsMap(fieldsMap, log);

					if (crashLogFilter.checkFlag(log)) {
						buildLogMsg(errorMsgs, log.getMsg(), log.getId());
						buildDistributions(log, distributions);
						totalCount++;
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

		info.setTotalCount(totalCount);
		info.setErrors(buildLogMsgList(errorMsgs));
		info.setDistributions(buildDistributionChart(distributions));

		if (!fieldsMap.isEmpty()) {
			info.setFieldsInfo(buildFiledsInfo(fieldsMap));
		}
	}

	public CrashLogDisplayInfo buildCrashLogDisplayInfo(CrashLogQueryEntity entity) {
		CrashLogDisplayInfo info = new CrashLogDisplayInfo();

		buildCrashLogData(entity, info);
		info.setAppNames(m_mobileConfigManager.queryApps());

		return info;
	}

	public CrashLogDisplayInfo buildCrashTrend(CrashLogQueryEntity entity1, CrashLogQueryEntity entity2) {
		CrashLogDisplayInfo info = new CrashLogDisplayInfo();
		Map<String, Set<String>> fieldsMap = new HashMap<String, Set<String>>();
		Double[] current = getCrashTrendData(entity1, fieldsMap);
		Double[] comparison = null;

		if (entity2 != null) {
			comparison = getCrashTrendData(entity2, fieldsMap);
		}
		LineChart lineChart = buildLineChart(current, comparison);

		info.setLineChart(lineChart);

		if (!fieldsMap.isEmpty()) {
			info.setFieldsInfo(buildFiledsInfo(fieldsMap));
		}
		info.setAppNames(m_mobileConfigManager.queryApps());
		return info;
	}

	private void buildDistributions(CrashLog log, Map<String, Map<String, AtomicInteger>> distributions) {
		if (distributions.isEmpty()) {
			Map<String, AtomicInteger> appVersions = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> platVersions = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> modules = new HashMap<String, AtomicInteger>();
			Map<String, AtomicInteger> devices = new HashMap<String, AtomicInteger>();

			distributions.put(APP_VERSIONS, appVersions);
			distributions.put(PLATFORM_VERSIONS, platVersions);
			distributions.put(MODULES, modules);
			distributions.put(DEVICES, devices);
		}

		addCount(log.getAppVersion(), distributions.get(APP_VERSIONS));
		addCount(log.getPlatformVersion(), distributions.get(PLATFORM_VERSIONS));
		addCount(log.getModule(), distributions.get(MODULES));
		addCount(log.getDeviceBrand() + "-" + log.getDeviceModel(), distributions.get(DEVICES));
	}

	private void buildFieldsMap(Map<String, Set<String>> fieldsMap, CrashLog log) {
		findOrCreate(APP_VERSIONS, fieldsMap).add(log.getAppVersion());
		findOrCreate(PLATFORM_VERSIONS, fieldsMap).add(log.getPlatformVersion());
		findOrCreate(MODULES, fieldsMap).add(log.getModule());
		findOrCreate(LEVELS, fieldsMap).add(Level.getNameByCode(log.getLevel()));
		findOrCreate(DEVICES, fieldsMap).add(log.getDeviceBrand() + "-" + log.getDeviceModel());
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
		List<String> m = new ArrayList<String>(fieldsMap.get(MODULES));
		List<String> d = new ArrayList<String>(fieldsMap.get(DEVICES));

		Collections.sort(v, comparator);
		Collections.sort(p, comparator);

		fieldsInfo.setAppVersions(v).setPlatVersions(p).setModules(m).setLevels(l).setDevices(d);

		return fieldsInfo;
	}

	private LineChart buildLineChart(Double[] current, Double[] comparison) {
		LineChart lineChart = new LineChart();
		lineChart.setId(Constants.APP);
		lineChart.setHtmlTitle("Crash数 (个/5分钟)");
		lineChart.add(Constants.CURRENT_STR, current);
		lineChart.add(Constants.COMPARISION_STR, comparison);
		return lineChart;
	}

	private boolean check(String condition, String value) {
		if (StringUtils.isBlank(condition) || condition.equals(value)) {
			return true;
		} else {
			return false;
		}
	}

	private Double[] getCrashTrendData(CrashLogQueryEntity entity, Map<String, Set<String>> fieldsMap) {
		Date startTime = entity.buildTrendStartTime();
		Date endTime = entity.buildEndTime();
		String appName = entity.getAppName();
		String appVersion = entity.getAppVersion();
		String platVersion = entity.getPlatformVersion();
		String module = entity.getModule();
		int platform = entity.getPlatform();
		long day = entity.buildDay().getTime();
		long step = TimeHelper.ONE_MINUTE * 5;
		int duration = (int) ((endTime.getTime() - day) / step);
		Double[] data = new Double[duration];
		int offset = 0;

		try {
			while (true) {
				List<CrashLog> result = m_crashLogDao.findDataByConditions(startTime, endTime, appName, platform, null,
				      offset, LIMIT, CrashLogEntity.READSET_FULL);

				for (CrashLog log : result) {
					if (check(appVersion, log.getAppVersion()) && check(platVersion, log.getPlatformVersion())
					      && check(module, log.getModule())) {
						Date date = log.getCrashTime();
						int index = (int) ((date.getTime() - day) / step);
						Double minuteData = data[index];

						if (minuteData == null) {
							data[index] = new Double(0);
						}
						data[index]++;
					}
					buildFieldsMap(fieldsMap, log);
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
		return data;
	}

	public CrashLogDetailInfo queryCrashLogDetailInfo(int id) {
		CrashLogDetailInfo info = new CrashLogDetailInfo();

		try {
			CrashLog crashLog = m_crashLogDao.findByPK(id, CrashLogEntity.READSET_FULL);
			int tag = crashLog.getTag();

			if (tag == Status.NOT_MAPPED.getStatus() || tag == Status.FAILED.getStatus()) {
				try {
					String url = m_crashLogConfig.findServerUrl(MAPPER) + "&id=" + id;
					InputStream in = Urls.forIO().readTimeout(5000).connectTimeout(1000).openStream(url);
					Files.forIO().readFrom(in, "utf-8");
				} catch (Exception e) {
					Cat.logError(e);
				}
			}

			info.setAppName(m_mobileConfigManager.getAppName(Integer.valueOf(crashLog.getAppName())));
			info.setPlatform(m_mobileConfigManager.getPlatformStr(crashLog.getPlatform()));
			info.setAppVersion(crashLog.getAppVersion());
			info.setPlatformVersion(crashLog.getPlatformVersion());
			info.setModule(crashLog.getModule());
			info.setLevel(Level.getNameByCode(crashLog.getLevel()));
			info.setDeviceBrand(crashLog.getDeviceBrand());
			info.setDeviceModel(crashLog.getDeviceModel());
			info.setCrashTime(crashLog.getCrashTime());
			info.setDpid(crashLog.getDpid());

			CrashLogContent detail = m_crashLogContentDao.findByPK(id, CrashLogContentEntity.READSET_FULL);
			byte[] compressed = detail.getContentMapped();

			if (compressed == null || compressed.length == 0) {
				compressed = detail.getContent();
			}
			info.setDetail(buildContent(compressed));
		} catch (Exception e) {
			Cat.logError(e);
		}

		return info;
	}

}