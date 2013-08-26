package com.dianping.cat.report.page.bug;

import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.transform.BaseVisitor;

public class UtilizationReportScore extends BaseVisitor {

	@Override
	public void visitDomain(Domain domain) {
		long urlCount = domain.getUrlCount();
		long serviceCount = domain.getServiceCount();

		if (urlCount > 0) {
			domain.setWebScore(computeWebScore(domain));
		}
		if (serviceCount > 0) {
			domain.setServiceScore(computeServiceScore(domain));
		}
	}

	private int computeWebScore(Domain domain) {
		long urlCount = domain.getUrlCount();
		long cacheCount = domain.getMemcacheCount();
		long sqlCount = domain.getSqlCount();
		long callCount = domain.getPigeonCallCount();
		long swallowCount = domain.getSwallowCallCount();

		return (int) ((callCount * 100 + sqlCount * 10 + cacheCount + swallowCount * 10) * 1.0 / urlCount);
	}

	private int computeServiceScore(Domain domain) {

		long serviceCount = domain.getServiceCount();
		long cacheCount = domain.getMemcacheCount();
		long sqlCount = domain.getSqlCount();
		long callCount = domain.getPigeonCallCount();
		long swallowCount = domain.getSwallowCallCount();

		return (int) ((callCount * 100 + sqlCount * 10 + cacheCount + swallowCount * 10) * 1.0 / serviceCount);
	}
}
