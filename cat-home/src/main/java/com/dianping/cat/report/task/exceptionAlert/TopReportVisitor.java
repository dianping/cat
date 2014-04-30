package com.dianping.cat.report.task.exceptionAlert;

import java.util.Date;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.BaseVisitor;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.alertReport.entity.AlertReport;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.system.config.ExceptionThresholdConfigManager;

public class TopReportVisitor extends BaseVisitor {

	private ExceptionThresholdConfigManager m_configManager;

	private AlertReport m_report;

	private Date m_currentStart;

	private String m_currentDomain;

	private Integer m_currentMinute;

	private long m_currentTime = System.currentTimeMillis();

	public TopReportVisitor(ExceptionThresholdConfigManager configManager) {
		m_configManager = configManager;
	}

	public TopReportVisitor setReport(AlertReport report) {
		m_report = report;
		return this;
	}

	@Override
	public void visitDomain(Domain domain) {
		m_currentDomain = domain.getName();
		super.visitDomain(domain);
	}

	@Override
	public void visitError(Error error) {
		long time = m_currentStart.getTime() + m_currentMinute * TimeUtil.ONE_MINUTE;
		int warnLimit = 0;
		int errorLimit = 0;
		if (m_configManager != null) {
			ExceptionLimit exceptionLimit = m_configManager.queryDomainExceptionLimit(m_currentDomain, error.getId());
			if (exceptionLimit == null) {
				exceptionLimit = m_configManager.queryDomainTotalLimit(m_currentDomain);
			}
			if (exceptionLimit != null) {
				warnLimit = exceptionLimit.getWarning();
				errorLimit = exceptionLimit.getError();
			}
		}

		if (time <= m_currentTime + TimeUtil.ONE_MINUTE) {
			int count = error.getCount();
			if (errorLimit > 0 & warnLimit > 0 & count < Math.min(warnLimit, errorLimit)) {
				return;
			}
			com.dianping.cat.home.alertReport.entity.Domain domain = m_report.findOrCreateDomain(m_currentDomain);
			domain.setName(m_currentDomain);
			com.dianping.cat.home.alertReport.entity.Exception exception = domain.findOrCreateException(error.getId());

			if (errorLimit > 0 && count >= errorLimit) {
				int exceptionError = exception.getErrorNumber();
				int domainError = domain.getErrorNumber();

				exception.setErrorNumber(exceptionError + 1);
				domain.setErrorNumber(domainError + 1);
			} else if (warnLimit > 0 && count >= warnLimit) {
				int exceptionWarn = exception.getWarnNumber();
				int domainWarn = domain.getWarnNumber();

				exception.setWarnNumber(exceptionWarn + 1);
				domain.setWarnNumber(domainWarn + 1);
			}
		}
	}

	@Override
	public void visitSegment(Segment segment) {
		m_currentMinute = segment.getId();
		super.visitSegment(segment);
	}

	@Override
	public void visitTopReport(TopReport topReport) {
		m_currentStart = topReport.getStartTime();
		super.visitTopReport(topReport);
	}
}
