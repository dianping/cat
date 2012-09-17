package com.dianping.cat.report.task.monthreport;

import java.util.Date;
import java.util.Map;

import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.monthreport.model.entity.BaseCacheInfo;
import com.dianping.cat.consumer.monthreport.model.entity.BaseInfo;
import com.dianping.cat.consumer.monthreport.model.entity.Call;
import com.dianping.cat.consumer.monthreport.model.entity.KvdbCache;
import com.dianping.cat.consumer.monthreport.model.entity.MemCache;
import com.dianping.cat.consumer.monthreport.model.entity.MonthReport;
import com.dianping.cat.consumer.monthreport.model.entity.ProblemInfo;
import com.dianping.cat.consumer.monthreport.model.entity.Service;
import com.dianping.cat.consumer.monthreport.model.entity.Sql;
import com.dianping.cat.consumer.monthreport.model.entity.Url;
import com.dianping.cat.consumer.monthreport.model.entity.WebCache;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.transaction.model.entity.Machine;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.report.page.problem.ProblemStatistics;
import com.dianping.cat.report.page.problem.ProblemStatistics.TypeStatistics;

public class MonthReportBuilder {

	private MonthReport m_monthReport = new MonthReport();

	public MonthReport build(TransactionReport transactionReport, EventReport eventReport, ProblemReport problemReport) {
		buildReportInfo(transactionReport);
		buildProblemInfo(problemReport);
		buildTansactionInfo(transactionReport);
		buildCacheInfo(transactionReport, eventReport);

		buildProblemPercent();
		return m_monthReport;
	}

	private void buildProblemPercent() {
		ProblemInfo info = m_monthReport.getProblemInfo();
		long urlCount = m_monthReport.getUrl().getBaseInfo().getTotal();
		long serviceCount = m_monthReport.getService().getBaseInfo().getTotal();
		long sqlCount = m_monthReport.getSql().getBaseInfo().getTotal();
		long webCacheCount = m_monthReport.getWebCache().getBaseCacheInfo().getTotal();
		long kvdbCacheCount = m_monthReport.getKvdbCache().getBaseCacheInfo().getTotal();
		long memCacheCount = m_monthReport.getMemCache().getBaseCacheInfo().getTotal();

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

	private void buildProblemInfo(ProblemReport problemReport) {
		int days = m_monthReport.getDay();
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
			double avgExceptions = exceptionCounts / days;
			info.setExceptions(exceptionCounts);
			info.setAvgExceptions(avgExceptions);
		}

		TypeStatistics longUrl = status.get("long-url");
		if (longUrl != null) {
			long longUrls = longUrl.getCount();
			double avgLongUrls = longUrls / days;
			info.setLongUrls(longUrls);
			info.setAvgLongUrls(avgLongUrls);
		}

		TypeStatistics longSql = status.get("long-sql");
		if (longSql != null) {
			long longSqls = longSql.getCount();
			double avgLongSqls = longSqls / days;
			info.setLongSqls(longSqls);
			info.setAvgLongSqls(avgLongSqls);
		}

		TypeStatistics longCache = status.get("long-cache");
		if (longCache != null) {
			long longCacheCounts = longCache.getCount();
			double avgLongCaches = longCacheCounts / days;
			info.setLongCaches(longCacheCounts);
			info.setAvgLongCaches(avgLongCaches);
		}

		TypeStatistics longService = status.get("long-service");
		if (longService != null) {
			long longServices = longService.getCount();
			double avgLongServices = longServices / days;
			info.setLongServices(longServices);
			info.setAvgLongServices(avgLongServices);
		}

		m_monthReport.setProblemInfo(info);
	}

	private BaseCacheInfo buildBaseCacheInfo(TransactionReport transactionReport, EventReport eventReport, String type) {
		TransactionType transactionType = transactionReport.findOrCreateMachine("All").findOrCreateType(type);
		EventType eventType = eventReport.findOrCreateMachine("All").findOrCreateType(type);
		int days = m_monthReport.getDay();

		BaseCacheInfo info = new BaseCacheInfo();
		if (transactionType != null) {
			long totalCount = transactionType.getTotalCount();
			info.setTotal(totalCount);
			info.setResponseTime(transactionType.getAvg());
			info.setAvg((double) totalCount / days);

			if (eventType != null) {
				long missed = eventType.getTotalCount();
				if (totalCount > 0) {
					info.setHitPercent(1 - (double) missed / totalCount);
				}
			}
		}
		return info;
	}

	private void buildCacheInfo(TransactionReport transactionReport, EventReport eventReport) {
		WebCache webCache = new WebCache();
		webCache.setBaseCacheInfo(buildBaseCacheInfo(transactionReport, eventReport, "Cache.web"));
		m_monthReport.setWebCache(webCache);

		KvdbCache kvdbCache = new KvdbCache();
		kvdbCache.setBaseCacheInfo(buildBaseCacheInfo(transactionReport, eventReport, "Cache.kvdb"));
		m_monthReport.setKvdbCache(kvdbCache);

		MemCache memCache = new MemCache();
		memCache.setBaseCacheInfo(buildBaseCacheInfo(transactionReport, eventReport, "Cache.memcached"));
		m_monthReport.setMemCache(memCache);
	}

	private BaseInfo buildBaseInfo(TransactionType type, int days) {
		BaseInfo info = new BaseInfo();
		double responseTime = type.getAvg();
		long total = type.getTotalCount();
		double avg = total / days;
		long errorTotal = type.getFailCount();
		double errorAvg = errorTotal / days;

		if (total > 0) {
			double errorPercent = (double) errorTotal / (double) total;
			info.setErrorPercent(errorPercent);
			info.setSuccessPercent(1 - errorPercent);
		}
		info.setResponseTime(responseTime);
		info.setTotal(total);
		info.setAvg(avg);
		info.setErrorTotal(errorTotal);
		info.setErrorAvg(errorAvg);
		return info;
	}

	private void buildTansactionInfo(TransactionReport transactionReport) {
		Machine machine = transactionReport.getMachines().get("All");
		Map<String, TransactionType> types = machine.getTypes();
		int days = m_monthReport.getDay();

		TransactionType url = types.get("URL");
		if (url != null) {
			Url temp = new Url();
			BaseInfo urlBaseInfo = buildBaseInfo(url, days);
			temp.setBaseInfo(urlBaseInfo);
			m_monthReport.setUrl(temp);
		} else {
			Url temp = new Url();
			BaseInfo urlBaseInfo = new BaseInfo();
			temp.setBaseInfo(urlBaseInfo);
			m_monthReport.setUrl(temp);
		}

		TransactionType service = types.get("Service");
		if (service != null) {
			BaseInfo serviceBaseInfo = buildBaseInfo(service, days);
			Service temp = new Service();
			temp.setBaseInfo(serviceBaseInfo);
			m_monthReport.setService(temp);
		} else {
			Service temp = new Service();
			BaseInfo serviceBaseInfo = new BaseInfo();
			temp.setBaseInfo(serviceBaseInfo);
			m_monthReport.setService(temp);
		}

		TransactionType call = types.get("Call");
		if (call != null) {
			BaseInfo callBaseInfo = buildBaseInfo(call, days);
			Call temp = new Call();
			temp.setBaseInfo(callBaseInfo);
			m_monthReport.setCall(temp);
		} else {
			Call temp = new Call();
			BaseInfo callBaseInfo = new BaseInfo();
			temp.setBaseInfo(callBaseInfo);
			m_monthReport.setCall(temp);
		}

		TransactionType sql = types.get("SQL");
		if (sql != null) {
			BaseInfo sqlBaseInfo = buildBaseInfo(sql, days);
			Sql temp = new Sql();
			temp.setBaseInfo(sqlBaseInfo);
			m_monthReport.setSql(temp);
		} else {
			Sql temp = new Sql();
			BaseInfo sqlBaseInfo = new BaseInfo();
			temp.setBaseInfo(sqlBaseInfo);
			m_monthReport.setSql(temp);
		}
	}

	private void buildReportInfo(TransactionReport transactionReport) {
		long day = 24 * 60 * 60 * 1000L;
		Date startTime = transactionReport.getStartTime();
		Date endTime = transactionReport.getEndTime();

		m_monthReport.getDomains().addAll(transactionReport.getDomainNames());
		m_monthReport.setStartTime(startTime);
		m_monthReport.setEndTime(endTime);
		m_monthReport.setDomain(transactionReport.getDomain());
		m_monthReport.setDay((int) ((endTime.getTime() - startTime.getTime()) / day) + 1);
	}

}
