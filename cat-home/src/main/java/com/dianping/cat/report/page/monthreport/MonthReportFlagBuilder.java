package com.dianping.cat.report.page.monthreport;

import com.dianping.cat.Cat;
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
import com.dianping.cat.consumer.monthreport.model.transform.BaseVisitor;

public class MonthReportFlagBuilder extends BaseVisitor {
	private MonthReport m_lastMonthReport;

	private String m_currentCache;

	private String m_currentType;

	private static final int HIGH_GOOD = 1;

	private static final int HIGH_BAD = 2;

	private static final int LOW_GOOD = -1;

	private static final int LOW_BAD = -2;

	public MonthReportFlagBuilder setLastMonthReport(MonthReport report) {
		m_lastMonthReport = report;
		return this;
	}

	@Override
	public void visitBaseCacheInfo(BaseCacheInfo baseCacheInfo) {
		try {
	      BaseCacheInfo lastCache = null;
	      if (m_currentCache.equals("kvdbCache")) {
	      	lastCache = m_lastMonthReport.getKvdbCache().getBaseCacheInfo();
	      } else if (m_currentCache.equals("memCache")) {
	      	lastCache = m_lastMonthReport.getMemCache().getBaseCacheInfo();
	      } else if (m_currentCache.equals("webCache")) {
	      	lastCache = m_lastMonthReport.getWebCache().getBaseCacheInfo();
	      } else {
	      	throw new RuntimeException("inter error in build cache flag！");
	      }

	      if (baseCacheInfo.getTotal() > lastCache.getTotal()) {
	      	baseCacheInfo.setTotalFlag(HIGH_GOOD);
	      } else if (baseCacheInfo.getTotal() < lastCache.getTotal()) {
	      	baseCacheInfo.setTotalFlag(LOW_BAD);
	      }
	      if (baseCacheInfo.getAvg() > lastCache.getAvg()) {
	      	baseCacheInfo.setAvgFlag(HIGH_GOOD);
	      } else if (baseCacheInfo.getAvg() < lastCache.getAvg()) {
	      	baseCacheInfo.setAvgFlag(LOW_BAD);
	      }
	      if (baseCacheInfo.getResponseTime() > lastCache.getResponseTime()) {
	      	baseCacheInfo.setResponseTimeFlag(HIGH_BAD);
	      } else if (baseCacheInfo.getResponseTime() < lastCache.getResponseTime()) {
	      	baseCacheInfo.setResponseTimeFlag(LOW_GOOD);
	      }
	      if (baseCacheInfo.getHitPercent() > lastCache.getHitPercent()) {
	      	baseCacheInfo.setHitPercentFlag(HIGH_GOOD);
	      } else if (baseCacheInfo.getHitPercent() < lastCache.getHitPercent()) {
	      	baseCacheInfo.setHitPercentFlag(LOW_BAD);
	      }
      } catch (Exception e) {
      	Cat.logError(e);
      }

	}

	@Override
	public void visitBaseInfo(BaseInfo baseInfo) {
		try {
	      BaseInfo lastBaseInfo = null;
	      if (m_currentType.equals("Call")) {
	      	lastBaseInfo = m_lastMonthReport.getCall().getBaseInfo();
	      } else if (m_currentType.equals("URL")) {
	      	lastBaseInfo = m_lastMonthReport.getUrl().getBaseInfo();
	      } else if (m_currentType.equals("Sql")) {
	      	lastBaseInfo = m_lastMonthReport.getSql().getBaseInfo();
	      } else if (m_currentType.equals("Service")) {
	      	lastBaseInfo = m_lastMonthReport.getService().getBaseInfo();
	      } else {
	      	throw new RuntimeException("inter error in build baseInfo flag！");
	      }

	      if (baseInfo.getAvg() > lastBaseInfo.getAvg()) {
	      	baseInfo.setAvgFlag(HIGH_GOOD);
	      } else if (baseInfo.getAvg() < lastBaseInfo.getAvg()) {
	      	baseInfo.setAvgFlag(LOW_BAD);
	      }
	      if (baseInfo.getTotal() > lastBaseInfo.getTotal()) {
	      	baseInfo.setTotalFlag(HIGH_GOOD);
	      } else if (baseInfo.getTotal() < lastBaseInfo.getTotal()) {
	      	baseInfo.setTotalFlag(LOW_BAD);
	      }
	      if (baseInfo.getErrorTotal() > lastBaseInfo.getErrorTotal()) {
	      	baseInfo.setErrorTotalFlag(HIGH_BAD);
	      } else if (baseInfo.getErrorTotal() < lastBaseInfo.getErrorTotal()) {
	      	baseInfo.setErrorTotalFlag(LOW_GOOD);
	      }
	      if (baseInfo.getErrorAvg() > lastBaseInfo.getErrorAvg()) {
	      	baseInfo.setErrorAvgFlag(HIGH_BAD);
	      } else if (baseInfo.getErrorAvg() < lastBaseInfo.getErrorAvg()) {
	      	baseInfo.setErrorAvgFlag(LOW_GOOD);
	      }
	      if (baseInfo.getResponseTime() > lastBaseInfo.getResponseTime()) {
	      	baseInfo.setResponseTimeFlag(HIGH_BAD);
	      } else if (baseInfo.getResponseTime() < lastBaseInfo.getResponseTime()) {
	      	baseInfo.setResponseTimeFlag(LOW_GOOD);
	      }
	      if (baseInfo.getErrorPercent() > lastBaseInfo.getErrorPercent()) {
	      	baseInfo.setErrorPercentFlag(HIGH_BAD);
	      } else if (baseInfo.getErrorPercent() < lastBaseInfo.getErrorPercent()) {
	      	baseInfo.setErrorPercentFlag(LOW_GOOD);
	      }
	      if (baseInfo.getSuccessPercent() > lastBaseInfo.getSuccessPercent()) {
	      	baseInfo.setSuccessPercentFlag(HIGH_GOOD);
	      } else if (baseInfo.getSuccessPercent() < lastBaseInfo.getSuccessPercent()) {
	      	baseInfo.setSuccessPercentFlag(LOW_BAD);
	      }
      } catch (Exception e) {
      	Cat.logError(e);
      }
	}

	@Override
	public void visitCall(Call call) {
		m_currentType = "Call";
		super.visitCall(call);
	}

	@Override
	public void visitKvdbCache(KvdbCache kvdbCache) {
		m_currentCache = "kvdbCache";
		super.visitKvdbCache(kvdbCache);
	}

	@Override
	public void visitMemCache(MemCache memCache) {
		m_currentCache = "memCache";
		super.visitMemCache(memCache);
	}

	@Override
	public void visitMonthReport(MonthReport monthReport) {
		super.visitMonthReport(monthReport);
	}

	@Override
	public void visitProblemInfo(ProblemInfo problemInfo) {
		try {
	      ProblemInfo lastProblemInfo = m_lastMonthReport.getProblemInfo();
	      if (problemInfo.getExceptions() > lastProblemInfo.getExceptions()) {
	      	problemInfo.setExceptionsFlag(HIGH_BAD);
	      } else if (problemInfo.getExceptions() < lastProblemInfo.getExceptions()) {
	      	problemInfo.setExceptionsFlag(LOW_GOOD);
	      }
	      if (problemInfo.getAvgExceptions() > lastProblemInfo.getAvgExceptions()) {
	      	problemInfo.setAvgExceptionsFlag(HIGH_GOOD);
	      } else if (problemInfo.getAvgExceptions() < lastProblemInfo.getAvgExceptions()) {
	      	problemInfo.setAvgExceptionsFlag(LOW_BAD);
	      }
	      if (problemInfo.getLongSqls() > lastProblemInfo.getLongSqls()) {
	      	problemInfo.setLongSqlsFlag(HIGH_BAD);
	      } else if (problemInfo.getLongSqls() < lastProblemInfo.getLongSqls()) {
	      	problemInfo.setLongSqlsFlag(LOW_GOOD);
	      }
	      if (problemInfo.getAvgLongSqls() > lastProblemInfo.getAvgLongSqls()) {
	      	problemInfo.setAvgLongSqlsFlag(HIGH_BAD);
	      } else if (problemInfo.getAvgLongSqls() < lastProblemInfo.getAvgLongSqls()) {
	      	problemInfo.setAvgLongSqlsFlag(LOW_GOOD);
	      }
	      if (problemInfo.getLongSqlPercent() > lastProblemInfo.getLongSqlPercent()) {
	      	problemInfo.setLongSqlPercentFlag(HIGH_BAD);
	      } else if (problemInfo.getLongSqlPercent() < lastProblemInfo.getLongSqlPercent()) {
	      	problemInfo.setLongSqlPercentFlag(LOW_GOOD);
	      }
	      if (problemInfo.getLongUrls() > lastProblemInfo.getLongUrls()) {
	      	problemInfo.setLongUrlsFlag(HIGH_BAD);
	      } else if (problemInfo.getLongUrls() < lastProblemInfo.getLongUrls()) {
	      	problemInfo.setLongUrlsFlag(LOW_GOOD);
	      }
	      if (problemInfo.getAvgLongUrls() > lastProblemInfo.getAvgLongUrls()) {
	      	problemInfo.setAvgLongUrlsFlag(HIGH_BAD);
	      } else if (problemInfo.getAvgLongUrls() < lastProblemInfo.getAvgLongUrls()) {
	      	problemInfo.setAvgLongUrlsFlag(LOW_GOOD);
	      }
	      if (problemInfo.getLongUrlPercent() > lastProblemInfo.getLongUrlPercent()) {
	      	problemInfo.setLongUrlPercentFlag(HIGH_BAD);
	      } else if (problemInfo.getLongUrlPercent() < lastProblemInfo.getLongUrlPercent()) {
	      	problemInfo.setLongUrlPercentFlag(LOW_GOOD);
	      }
	      if (problemInfo.getLongCaches() > lastProblemInfo.getLongCaches()) {
	      	problemInfo.setLongCachesFlag(HIGH_BAD);
	      } else if (problemInfo.getLongCaches() < lastProblemInfo.getLongCaches()) {
	      	problemInfo.setLongCachesFlag(LOW_GOOD);
	      }
	      if (problemInfo.getAvgLongCaches() > lastProblemInfo.getAvgLongCaches()) {
	      	problemInfo.setAvgLongCachesFlag(HIGH_BAD);
	      } else if (problemInfo.getAvgLongCaches() < lastProblemInfo.getAvgLongCaches()) {
	      	problemInfo.setAvgLongCachesFlag(LOW_GOOD);
	      }
	      if (problemInfo.getLongCachePercent() > lastProblemInfo.getLongCachePercent()) {
	      	problemInfo.setLongCachePercentFlag(HIGH_BAD);
	      } else if (problemInfo.getLongCachePercent() < lastProblemInfo.getLongCachePercent()) {
	      	problemInfo.setLongCachePercentFlag(LOW_GOOD);
	      }
	      if (problemInfo.getLongServices() > lastProblemInfo.getLongServices()) {
	      	problemInfo.setLongServicesFlag(HIGH_BAD);
	      } else if (problemInfo.getLongServices() < lastProblemInfo.getLongServices()) {
	      	problemInfo.setLongServicesFlag(LOW_GOOD);
	      }
	      if (problemInfo.getAvgLongServices() > lastProblemInfo.getAvgLongServices()) {
	      	problemInfo.setAvgLongServicesFlag(HIGH_BAD);
	      } else if (problemInfo.getAvgLongServices() < lastProblemInfo.getAvgLongServices()) {
	      	problemInfo.setAvgLongServicesFlag(LOW_GOOD);
	      }
	      if (problemInfo.getLongServicePercent() > lastProblemInfo.getLongServicePercent()) {
	      	problemInfo.setLongServicePercentFlag(HIGH_BAD);
	      } else if (problemInfo.getLongServicePercent() < lastProblemInfo.getLongServicePercent()) {
	      	problemInfo.setLongServicePercentFlag(LOW_GOOD);
	      }
      } catch (Exception e) {
      	Cat.logError(e);
      }
	}

	@Override
	public void visitService(Service service) {
		m_currentType = "Service";
		super.visitService(service);
	}

	@Override
	public void visitSql(Sql sql) {
		m_currentType = "Sql";
		super.visitSql(sql);
	}

	@Override
	public void visitUrl(Url url) {
		m_currentType = "URL";
		super.visitUrl(url);
	}

	@Override
	public void visitWebCache(WebCache webCache) {
		m_currentCache = "webCache";
		super.visitWebCache(webCache);
	}
}
