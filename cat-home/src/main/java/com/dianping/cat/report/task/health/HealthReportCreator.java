package com.dianping.cat.report.task.health;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.health.model.entity.BaseCacheInfo;
import com.dianping.cat.consumer.health.model.entity.BaseInfo;
import com.dianping.cat.consumer.health.model.entity.Call;
import com.dianping.cat.consumer.health.model.entity.ClientService;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.health.model.entity.KvdbCache;
import com.dianping.cat.consumer.health.model.entity.MachineInfo;
import com.dianping.cat.consumer.health.model.entity.MemCache;
import com.dianping.cat.consumer.health.model.entity.ProblemInfo;
import com.dianping.cat.consumer.health.model.entity.Service;
import com.dianping.cat.consumer.health.model.entity.Sql;
import com.dianping.cat.consumer.health.model.entity.Url;
import com.dianping.cat.consumer.health.model.entity.WebCache;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.CatString;
import com.dianping.cat.report.page.problem.ProblemStatistics;
import com.dianping.cat.report.page.problem.ProblemStatistics.TypeStatistics;
import com.dianping.cat.report.task.health.HealthServiceCollector.ServiceInfo;

public class HealthReportCreator {

	private HealthReport m_healthReport;

	public HealthReport build(TransactionReport transactionReport, EventReport eventReport, ProblemReport problemReport,
	      HeartbeatReport heartbeatReport, Map<String, ServiceInfo> infos) {

		m_healthReport = new HealthReport(transactionReport.getDomain());

		buildReportInfo(transactionReport);
		buildProblemInfo(problemReport);
		buildTansactionInfo(transactionReport);
		buildCacheInfo(transactionReport, eventReport);
		buildMachinInfo(heartbeatReport);
		buildProblemPercent();
		buildClientServiceInfo(infos);
		return m_healthReport;
	}

	private BaseCacheInfo buildBaseCacheInfo(TransactionReport transactionReport, EventReport eventReport, String type) {
		Map<String, TransactionType> transactionTypes = transactionReport.findOrCreateMachine(CatString.ALL)
		      .getTypes();
		Map<String, EventType> eventTypes = eventReport.findOrCreateMachine(CatString.ALL).getTypes();

		List<TransactionType> transactionTypeList = new ArrayList<TransactionType>();
		List<EventType> eventTypeList = new ArrayList<EventType>();

		for (Entry<String, TransactionType> transactionEntry : transactionTypes.entrySet()) {
			String key = transactionEntry.getKey();

			if (key.equals(type) || key.startsWith(type)) {
				transactionTypeList.add(transactionEntry.getValue());
			}
		}
		for (Entry<String, EventType> eventEntry : eventTypes.entrySet()) {
			String key = eventEntry.getKey();

			if (key.equals(type) || key.startsWith(type)) {
				eventTypeList.add(eventEntry.getValue());
			}
		}

		long totalCount = 0;
		long missCount = 0;
		double timeSum = 0;

		for (TransactionType temp : transactionTypeList) {
			for (TransactionName name : temp.getNames().values()) {
				String id = name.getId();

				if (id.endsWith("get") || id.endsWith("mGet")) {
					totalCount += name.getTotalCount();
					timeSum += name.getTotalCount() * name.getAvg();
				}
			}
		}
		for (EventType temp : eventTypeList) {
			missCount += temp.getTotalCount();
		}

		BaseCacheInfo info = new BaseCacheInfo();

		if (totalCount > 0) {
			info.setTotal(totalCount);
			info.setResponseTime(timeSum / totalCount);
			info.setHitPercent(1 - (double) missCount / totalCount);
		}
		return info;
	}

	private BaseInfo buildBaseInfo(TransactionType type) {
		BaseInfo info = new BaseInfo();
		double responseTime = type.getAvg();
		long total = type.getTotalCount();
		// double avg = total / days;
		long errorTotal = type.getFailCount();
		// double errorAvg = errorTotal / days;

		if (total > 0) {
			double errorPercent = (double) errorTotal / (double) total;
			info.setErrorPercent(errorPercent);
			info.setSuccessPercent(1 - errorPercent);
		}
		info.setResponseTime(responseTime);
		info.setTotal(total);
		// info.setAvg(avg);
		info.setErrorTotal(errorTotal);
		// info.setErrorAvg(errorAvg);
		return info;
	}

	private void buildCacheInfo(TransactionReport transactionReport, EventReport eventReport) {
		WebCache webCache = new WebCache();
		webCache.setBaseCacheInfo(buildBaseCacheInfo(transactionReport, eventReport, "Cache.web"));
		m_healthReport.setWebCache(webCache);

		KvdbCache kvdbCache = new KvdbCache();
		kvdbCache.setBaseCacheInfo(buildBaseCacheInfo(transactionReport, eventReport, "Cache.kvdb"));
		m_healthReport.setKvdbCache(kvdbCache);

		MemCache memCache = new MemCache();
		memCache.setBaseCacheInfo(buildBaseCacheInfo(transactionReport, eventReport, "Cache.memcached"));
		m_healthReport.setMemCache(memCache);
	}

	private void buildClientServiceInfo(Map<String, ServiceInfo> infos) {
		BaseInfo info = new BaseInfo();

		ClientService clientService = new ClientService();
		m_healthReport.setClientService(clientService);
		m_healthReport.getClientService().setBaseInfo(info);

		String domain = m_healthReport.getDomain();
		ServiceInfo serviceInfo = infos.get(domain);
		if (serviceInfo != null) {
			info.setTotal(serviceInfo.getTotalCount());
			info.setErrorTotal(serviceInfo.getFailCount());
			info.setResponseTime(serviceInfo.getAvgTime());
			info.setErrorPercent(serviceInfo.getFailCount() / (double) serviceInfo.getTotalCount());
		}
	}

	private void buildMachinInfo(HeartbeatReport heartbeatReport) {
		MachineInfo info = new MachineInfo();

		int number = heartbeatReport.getMachines().size();
		info.setNumbers(number);

		buildMachinInfos(info, heartbeatReport);
		m_healthReport.setMachineInfo(info);
	}

	private void buildMachinInfos(MachineInfo info, HeartbeatReport heartBeatReport) {
		Map<String, Double> loads = new HashMap<String, Double>();
		Map<String, Double> gcs = new HashMap<String, Double>();
		Map<String, Double> https = new HashMap<String, Double>();
		Map<String, Double> pigeons = new HashMap<String, Double>();
		Map<String, Double> memory = new HashMap<String, Double>();

		Collection<com.dianping.cat.consumer.heartbeat.model.entity.Machine> machines = heartBeatReport.getMachines()
		      .values();
		for (com.dianping.cat.consumer.heartbeat.model.entity.Machine machine : machines) {
			double loadTotal = 0;
			double oldgcTotal = 0;
			double httpTotal = 0;
			double pgieonTotal = 0;
			double memoryTotal = 0;
			String ip = machine.getIp();
			int i = 0;

			List<Period> periods = machine.getPeriods();
			oldgcTotal = queryOldgcNumber(machine);

			for (; i < periods.size(); i++) {
				loadTotal += periods.get(i).getSystemLoadAverage();
				httpTotal += periods.get(i).getHttpThreadCount();
				pgieonTotal += periods.get(i).getPigeonThreadCount();
				memoryTotal += periods.get(i).getHeapUsage() / (double) (1024) / (double) (1204);
			}
			loads.put(ip, loadTotal / i);
			gcs.put(ip, oldgcTotal);
			https.put(ip, httpTotal / i);
			pigeons.put(ip, pgieonTotal / i);
			memory.put(ip, memoryTotal / i);
		}

		MapEntry entry = findMaxEntry(loads);

		info.setAvgLoad(entry.getAvg());
		info.setAvgMaxLoad(entry.getMax());
		info.setAvgMaxLoadMachine(entry.getIp());
		info.setAvgLoadCount(1);
		info.setAvgLoadSum(entry.getAvg());

		entry = findMaxEntry(gcs);

		info.setAvgOldgc(entry.getAvg());
		info.setAvgMaxOldgc(entry.getMax());
		info.setAvgMaxOldgcMachine(entry.getIp());
		info.setAvgOldgcCount(1);
		info.setAvgOldgcSum(entry.getAvg());

		entry = findMaxEntry(https);

		info.setAvgHttp(entry.getAvg());
		info.setAvgMaxHttp(entry.getMax());
		info.setAvgMaxHttpMachine(entry.getIp());
		info.setAvgHttpCount(1);
		info.setAvgHttpSum(entry.getAvg());

		entry = findMaxEntry(pigeons);

		info.setAvgPigeon(entry.getAvg());
		info.setAvgMaxPigeon(entry.getMax());
		info.setAvgMaxPigeonMachine(entry.getIp());
		info.setAvgPigeonCount(1);
		info.setAvgPigeonSum(entry.getAvg());

		entry = findMaxEntry(memory);

		info.setAvgMemoryUsed(entry.getAvg());
		info.setAvgMaxMemoryUsed(entry.getMax());
		info.setAvgMaxMemoryUsedMachine(entry.getIp());
		info.setAvgMemoryUsedCount(1);
		info.setAvgMemoryUsedSum(entry.getAvg());

	}
	
	private void buildProblemInfo(ProblemReport problemReport) {
		// int days = m_healthReport.getDay();
		ProblemInfo info = new ProblemInfo();
		ProblemStatistics statistics = new ProblemStatistics();

		statistics.setSqlThreshold(0);
		statistics.setUrlThreshold(0);
		statistics.setServiceThreshold(0);

		statistics.setAllIp(true);
		statistics.visitProblemReport(problemReport);

		Map<String, TypeStatistics> status = statistics.getStatus();
		TypeStatistics exceptions = status.get("error");
		if (exceptions != null) {
			long exceptionCounts = exceptions.getCount();
			// double avgExceptions = exceptionCounts / days;
			info.setExceptions(exceptionCounts);
			// info.setAvgExceptions(avgExceptions);
		}

		TypeStatistics longUrl = status.get("long-url");
		if (longUrl != null) {
			long longUrls = longUrl.getCount();
			// double avgLongUrls = longUrls / days;
			info.setLongUrls(longUrls);
			// info.setAvgLongUrls(avgLongUrls);
		}

		TypeStatistics longSql = status.get("long-sql");
		if (longSql != null) {
			long longSqls = longSql.getCount();
			// double avgLongSqls = longSqls / days;
			info.setLongSqls(longSqls);
			// info.setAvgLongSqls(avgLongSqls);
		}

		TypeStatistics longCache = status.get("long-cache");
		if (longCache != null) {
			long longCacheCounts = longCache.getCount();
			// double avgLongCaches = longCacheCounts / days;
			info.setLongCaches(longCacheCounts);
			// info.setAvgLongCaches(avgLongCaches);
		}

		TypeStatistics longService = status.get("long-service");
		if (longService != null) {
			long longServices = longService.getCount();
			// double avgLongServices = longServices / days;
			info.setLongServices(longServices);
			// info.setAvgLongServices(avgLongServices);
		}

		m_healthReport.setProblemInfo(info);
	}

	private void buildProblemPercent() {
		ProblemInfo info = m_healthReport.getProblemInfo();
		long urlCount = m_healthReport.getUrl().getBaseInfo().getTotal();
		long serviceCount = m_healthReport.getService().getBaseInfo().getTotal();
		long sqlCount = m_healthReport.getSql().getBaseInfo().getTotal();
		long webCacheCount = m_healthReport.getWebCache().getBaseCacheInfo().getTotal();
		long kvdbCacheCount = m_healthReport.getKvdbCache().getBaseCacheInfo().getTotal();
		long memCacheCount = m_healthReport.getMemCache().getBaseCacheInfo().getTotal();

		long longUrl = info.getLongUrls();
		long longService = info.getLongServices();
		long longSql = info.getLongSqls();
		long longCache = info.getLongCaches();

		if (urlCount > 0) {
			info.setLongUrlPercent((double) longUrl / urlCount);
		}
		if (serviceCount > 0) {
			info.setLongServicePercent((double) longService / serviceCount);
		}
		if (sqlCount > 0) {
			info.setLongSqlPercent((double) longSql / sqlCount);
		}
		if (longCache > 0) {
			info.setLongCachePercent((double) longCache / (webCacheCount + kvdbCacheCount + memCacheCount));
		}
	}

	private void buildReportInfo(TransactionReport transactionReport) {
		Date startTime = transactionReport.getStartTime();
		Date endTime = transactionReport.getEndTime();

		m_healthReport.getDomainNames().addAll(transactionReport.getDomainNames());
		m_healthReport.setStartTime(startTime);
		m_healthReport.setEndTime(endTime);
		m_healthReport.setDomain(transactionReport.getDomain());
	}

	private void buildTansactionInfo(TransactionReport transactionReport) {
		Machine machine = transactionReport.findOrCreateMachine(CatString.ALL);
		Map<String, TransactionType> types = machine.getTypes();

		TransactionType url = types.get("URL");
		if (url != null) {
			Url temp = new Url();
			BaseInfo urlBaseInfo = buildBaseInfo(url);
			temp.setBaseInfo(urlBaseInfo);
			m_healthReport.setUrl(temp);
		} else {
			Url temp = new Url();
			BaseInfo urlBaseInfo = new BaseInfo();
			temp.setBaseInfo(urlBaseInfo);
			m_healthReport.setUrl(temp);
		}

		TransactionType service = types.get("PigeonService");
		if (service == null) {
			service = types.get("Service");
		}
		if (service != null) {
			BaseInfo serviceBaseInfo = buildBaseInfo(service);
			Service temp = new Service();
			temp.setBaseInfo(serviceBaseInfo);
			m_healthReport.setService(temp);
		} else {
			Service temp = new Service();
			BaseInfo serviceBaseInfo = new BaseInfo();
			temp.setBaseInfo(serviceBaseInfo);
			m_healthReport.setService(temp);
		}

		TransactionType call = types.get("PigeonCall");
		if (call == null) {
			call = types.get("Call");
		}
		if (call != null) {
			BaseInfo callBaseInfo = buildBaseInfo(call);
			Call temp = new Call();
			temp.setBaseInfo(callBaseInfo);
			m_healthReport.setCall(temp);
		} else {
			Call temp = new Call();
			BaseInfo callBaseInfo = new BaseInfo();
			temp.setBaseInfo(callBaseInfo);
			m_healthReport.setCall(temp);
		}

		TransactionType sql = types.get("SQL");
		if (sql != null) {
			BaseInfo sqlBaseInfo = buildBaseInfo(sql);
			Sql temp = new Sql();
			temp.setBaseInfo(sqlBaseInfo);
			m_healthReport.setSql(temp);
		} else {
			Sql temp = new Sql();
			BaseInfo sqlBaseInfo = new BaseInfo();
			temp.setBaseInfo(sqlBaseInfo);
			m_healthReport.setSql(temp);
		}
	}

	private MapEntry findMaxEntry(Map<String, Double> maps) {
		double max = 0;
		double total = 0;
		String ip = "";
		for (Entry<String, Double> load : maps.entrySet()) {
			if (load.getValue() > max) {
				ip = load.getKey();
				max = load.getValue();
			}
			total = total + load.getValue();
		}
		MapEntry result = new MapEntry();

		if (maps.size() > 0) {
			result.setAvg(total / maps.size());
		}
		result.setIp(ip).setMax(max);
		return result;
	}

	private double queryOldgcNumber(com.dianping.cat.consumer.heartbeat.model.entity.Machine machine){
		double oldgcTotal =0;
		List<Period> periods = machine.getPeriods();
		long l = periods.get(periods.size() - 1).getOldGcCount() - periods.get(0).getOldGcCount();
		if (l >= 0) {
			oldgcTotal = l;
		} else {
			oldgcTotal = periods.get(periods.size() - 1).getOldGcCount();
		}
		return oldgcTotal;
	}

	static class MapEntry {
		private double m_avg;

		private String m_ip;

		private double m_max;

		public double getAvg() {
			return m_avg;
		}

		public String getIp() {
			return m_ip;
		}

		public double getMax() {
			return m_max;
		}

		public MapEntry setAvg(double avg) {
			m_avg = avg;
			return this;
		}

		public MapEntry setIp(String ip) {
			m_ip = ip;
			return this;
		}

		public MapEntry setMax(double max) {
			m_max = max;
			return this;
		}
	}

}
