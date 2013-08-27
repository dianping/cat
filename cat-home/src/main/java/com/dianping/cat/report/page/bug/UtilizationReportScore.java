package com.dianping.cat.report.page.bug;

import com.dianping.cat.home.utilization.config.entity.UtilizationConfig;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.transform.BaseVisitor;
import com.dianping.cat.system.config.UtilizationConfigManager;

public class UtilizationReportScore extends BaseVisitor {

	private UtilizationConfigManager m_configManager;

	private double m_callWeight;

	private double m_sqlWeight;

	private double m_cacheWeight;

	private double m_swallWeight;

	@Override
	public void visitDomain(Domain domain) {
		long urlCount = domain.getUrlCount();
		long serviceCount = domain.getServiceCount();

		if (urlCount > 0) {
			domain.setWebScore(computeScore(domain));
		}
		if (serviceCount > 0) {
			domain.setServiceScore(computeScore(domain));
		}
	}

	private int computeScore(Domain domain) {
		long urlCount = domain.getUrlCount();
		long serviceCount = domain.getServiceCount();
		long cacheCount = domain.getMemcacheCount();
		long sqlCount = domain.getSqlCount();
		long callCount = domain.getPigeonCallCount();
		long swallowCount = domain.getSwallowCallCount();

		return (int) ((callCount * m_callWeight + sqlCount * m_sqlWeight + cacheCount * m_cacheWeight + swallowCount
		      * m_swallWeight) * 1.0 / (urlCount + serviceCount));
	}

	public UtilizationReportScore setConfigManager(UtilizationConfigManager configManager) {
		m_configManager = configManager;

		UtilizationConfig utilizationConfig = m_configManager.getUtilizationConfig();
		m_callWeight = utilizationConfig.getPigeoncallWeight();
		m_sqlWeight = utilizationConfig.getSqlWeight();
		m_cacheWeight = utilizationConfig.getCacheWeight();
		m_swallWeight = utilizationConfig.getSwallowWeight();

		return this;
	}

}
