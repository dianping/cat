package com.dianping.cat.report.task.health;

import com.dianping.cat.consumer.health.model.entity.BaseCacheInfo;
import com.dianping.cat.consumer.health.model.entity.BaseInfo;
import com.dianping.cat.consumer.health.model.entity.HealthReport;
import com.dianping.cat.consumer.health.model.entity.MachineInfo;
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
		old.setNumbers(machineInfo.getNumbers());

		int avgLoadCount = old.getAvgLoadCount() + machineInfo.getAvgLoadCount();
		double avgLoadSum = old.getAvgLoadSum() + machineInfo.getAvgLoadSum();
		if (avgLoadCount > 0) {
			old.setAvgLoad(avgLoadSum / avgLoadCount);
		}

		int avgOldgcCount = old.getAvgOldgcCount() + machineInfo.getAvgOldgcCount();
		double avgOldgcSum = old.getAvgOldgcSum() + machineInfo.getAvgOldgcSum();
		if (avgOldgcCount > 0) {
			old.setAvgOldgc(avgOldgcSum / avgOldgcCount);
		}

		int avgHttpCount = old.getAvgHttpCount() + machineInfo.getAvgHttpCount();
		double avgHttpSum = old.getAvgHttpSum() + machineInfo.getAvgHttpSum();
		if (avgHttpCount > 0) {
			old.setAvgHttp(avgHttpSum / avgHttpCount);
		}

		int avgPigeonCount = old.getAvgPigeonCount() + machineInfo.getAvgPigeonCount();
		double avgPigeonSum = old.getAvgPigeonSum() + machineInfo.getAvgPigeonSum();
		if (avgPigeonCount > 0) {
			old.setAvgPigeon(avgPigeonSum / avgPigeonCount);
		}

		int avgMemoryUsedCount = old.getAvgMemoryUsedCount() + machineInfo.getAvgMemoryUsedCount();
		double avgMemoryUsedSum = old.getAvgMemoryUsedSum() + machineInfo.getAvgMemoryUsedSum();
		if (avgMemoryUsedCount > 0) {
			old.setAvgMemoryUsed(avgMemoryUsedSum / avgMemoryUsedCount);
		}
	}

	public HealthReportMerger setDuration(long time) {
		m_duration = time;
		return this;
	}

	@Override
	public void visitHealthReport(HealthReport healthReport) {
		super.visitHealthReport(healthReport);
	}

}
