package com.dianping.cat.report.task.health;

import com.dianping.cat.consumer.health.model.entity.BaseCacheInfo;
import com.dianping.cat.consumer.health.model.entity.BaseInfo;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.health.model.entity.MachineInfo;
import com.dianping.cat.consumer.health.model.entity.ProblemInfo;
import com.dianping.cat.consumer.health.model.transform.DefaultMerger;

public class HealthReportMerger extends DefaultMerger {
	private double m_duration = 0;

	public HealthReportMerger(HealthReport healthReport) {
		super(healthReport);
	}

	@Override
	protected void mergeBaseCacheInfo(BaseCacheInfo old, BaseCacheInfo baseCacheInfo) {
		double totalTime = old.getTotal() * old.getResponseTime() + baseCacheInfo.getTotal()
		      * baseCacheInfo.getResponseTime();
		long totalCount = old.getTotal() + baseCacheInfo.getTotal();
		double hitNumber = old.getTotal() * old.getHitPercent() + baseCacheInfo.getTotal()
		      * baseCacheInfo.getHitPercent();

		old.setTotal(totalCount);
		if (totalCount > 0) {
			old.setResponseTime(totalTime / totalCount);
			old.setHitPercent(hitNumber / totalCount);
		}
	}

	@Override
	protected void mergeBaseInfo(BaseInfo old, BaseInfo baseInfo) {
		long totalCount = old.getTotal() + baseInfo.getTotal();
		double totalTime = old.getTotal() * old.getResponseTime() + baseInfo.getTotal() * baseInfo.getResponseTime();
		long errorCount = old.getErrorTotal() + baseInfo.getErrorTotal();

		old.setTotal(totalCount);
		if (totalCount > 0) {
			old.setResponseTime(totalTime / totalCount);
			old.setErrorPercent((double) errorCount / totalCount);
			old.setSuccessPercent(1 - old.getErrorPercent());
		}
		old.setErrorTotal(errorCount);
		old.setTps(totalCount / m_duration);
	}

	@Override
	protected void mergeMachineInfo(MachineInfo old, MachineInfo machineInfo) {
		if (machineInfo.getNumbers() > 0) {
			old.setNumbers(machineInfo.getNumbers());
		}
		
		int avgLoadCount = old.getAvgLoadCount() + machineInfo.getAvgLoadCount();
		double avgLoadSum = old.getAvgLoadSum() + machineInfo.getAvgLoadSum();
		if (avgLoadCount > 0) {
			old.setAvgLoad(avgLoadSum / avgLoadCount);
		}
		old.setAvgLoadCount(avgLoadCount);
		old.setAvgLoadSum(avgLoadSum);

		int avgOldgcCount = old.getAvgOldgcCount() + machineInfo.getAvgOldgcCount();
		double avgOldgcSum = old.getAvgOldgcSum() + machineInfo.getAvgOldgcSum();
		if (avgOldgcCount > 0) {
			old.setAvgOldgc(avgOldgcSum / avgOldgcCount);
		}
		old.setAvgOldgcCount(avgOldgcCount);
		old.setAvgOldgcSum(avgOldgcSum);

		int avgHttpCount = old.getAvgHttpCount() + machineInfo.getAvgHttpCount();
		double avgHttpSum = old.getAvgHttpSum() + machineInfo.getAvgHttpSum();
		if (avgHttpCount > 0) {
			old.setAvgHttp(avgHttpSum / avgHttpCount);
		}
		old.setAvgHttpCount(avgHttpCount);
		old.setAvgHttpSum(avgHttpSum);

		int avgPigeonCount = old.getAvgPigeonCount() + machineInfo.getAvgPigeonCount();
		double avgPigeonSum = old.getAvgPigeonSum() + machineInfo.getAvgPigeonSum();
		if (avgPigeonCount > 0) {
			old.setAvgPigeon(avgPigeonSum / avgPigeonCount);
		}
		old.setAvgPigeonCount(avgPigeonCount);
		old.setAvgPigeonSum(avgPigeonSum);

		int avgMemoryUsedCount = old.getAvgMemoryUsedCount() + machineInfo.getAvgMemoryUsedCount();
		double avgMemoryUsedSum = old.getAvgMemoryUsedSum() + machineInfo.getAvgMemoryUsedSum();
		if (avgMemoryUsedCount > 0) {
			old.setAvgMemoryUsed(avgMemoryUsedSum / avgMemoryUsedCount);
		}
		old.setAvgMemoryUsedCount(avgMemoryUsedCount);
		old.setAvgMemoryUsedSum(avgMemoryUsedSum);

		if (machineInfo.getAvgMaxLoad() > old.getAvgMaxLoad()) {
			old.setAvgMaxLoad(machineInfo.getAvgMaxLoad());
			old.setAvgMaxLoadMachine(machineInfo.getAvgMaxLoadMachine());
		}
		if (machineInfo.getAvgMaxOldgc() > old.getAvgMaxOldgc()) {
			old.setAvgMaxOldgc(machineInfo.getAvgMaxOldgc());
			old.setAvgMaxOldgcMachine(machineInfo.getAvgMaxOldgcMachine());
		}
		if (machineInfo.getAvgMaxHttp() > old.getAvgMaxHttp()) {
			old.setAvgMaxHttp(machineInfo.getAvgMaxHttp());
			old.setAvgMaxHttpMachine(machineInfo.getAvgMaxHttpMachine());
		}
		if (machineInfo.getAvgMaxPigeon() > old.getAvgMaxPigeon()) {
			old.setAvgMaxPigeon(machineInfo.getAvgMaxPigeon());
			old.setAvgMaxPigeonMachine(machineInfo.getAvgMaxPigeonMachine());
		}
		if (machineInfo.getAvgMaxMemoryUsed() > old.getAvgMaxMemoryUsed()) {
			old.setAvgMaxMemoryUsed(machineInfo.getAvgMaxMemoryUsed());
			old.setAvgMaxMemoryUsedMachine(machineInfo.getAvgMaxMemoryUsedMachine());
		}
	}

	@Override
	protected void mergeProblemInfo(ProblemInfo old, ProblemInfo problemInfo) {
		old.setExceptions(old.getExceptions() + problemInfo.getExceptions());

		long longUrl = old.getLongUrls() + problemInfo.getLongUrls();
		double sum = 0;
		if (old.getLongUrlPercent() > 0) {
			sum += old.getLongUrls() / old.getLongUrlPercent();
		}
		if (problemInfo.getLongUrlPercent() > 0) {
			sum += problemInfo.getLongUrls() / problemInfo.getLongUrlPercent();
		}
		if (sum > 0) {
			old.setLongUrlPercent(longUrl / sum);
			old.setLongUrls(longUrl);
		}

		long longService = old.getLongServices() + problemInfo.getLongServices();
		sum = 0;
		if (old.getLongServicePercent() > 0) {
			sum += old.getLongServices() / old.getLongServicePercent();
		}
		if (problemInfo.getLongServicePercent() > 0) {
			sum += problemInfo.getLongServices() / problemInfo.getLongServicePercent();
		}
		if (sum > 0) {
			old.setLongServicePercent(longService / sum);
			old.setLongServices(longService);
		}

		long longCache = old.getLongCaches() + problemInfo.getLongCaches();
		sum = 0;
		if (old.getLongCachePercent() > 0) {
			sum += old.getLongCaches() / old.getLongCachePercent();
		}
		if (problemInfo.getLongCachePercent() > 0) {
			sum += problemInfo.getLongCaches() / problemInfo.getLongCachePercent();
		}
		if (sum > 0) {
			old.setLongCachePercent(longCache / sum);
			old.setLongCaches(longCache);
		}

		long longSql = old.getLongSqls() + problemInfo.getLongSqls();
		sum = 0;
		if (old.getLongSqlPercent() > 0) {
			sum += old.getLongSqls() / old.getLongSqlPercent();
		}
		if (problemInfo.getLongSqlPercent() > 0) {
			sum += problemInfo.getLongSqls() / problemInfo.getLongSqlPercent();
		}
		if (sum > 0) {
			old.setLongSqlPercent(longSql / sum);
			old.setLongSqls(longSql);
		}

	}

	public HealthReportMerger setDuration(long time) {
		m_duration = time;
		return this;
	}

	@Override
	public void visitHealthReport(HealthReport healthReport) {
		getHealthReport().getDomainNames().addAll((healthReport.getDomainNames()));
		super.visitHealthReport(healthReport);
	}

}
