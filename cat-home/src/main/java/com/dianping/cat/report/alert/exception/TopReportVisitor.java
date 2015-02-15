package com.dianping.cat.report.alert.exception;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.BaseVisitor;
import com.dianping.cat.home.alert.report.entity.AlertReport;
import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.system.config.ExceptionRuleConfigManager;

public class TopReportVisitor extends BaseVisitor {

	private ExceptionRuleConfigManager m_exceptionConfigManager;

	private AlertReport m_report;

	private String m_currentDomain;

	private long m_totalSegmentException;

	private static final String TOTAL_EXCEPTION_ALERT = "TotalExceptionAlert";

	@Inject
	private ServerConfigManager m_configManager;

	public TopReportVisitor setConfigManager(ServerConfigManager configManager) {
		m_configManager = configManager;
		return this;
	}

	public TopReportVisitor setExceptionRuleConfigManager(ExceptionRuleConfigManager exceptionConfigManager) {
		m_exceptionConfigManager = exceptionConfigManager;
		return this;
	}

	public TopReportVisitor setReport(AlertReport report) {
		m_report = report;
		return this;
	}

	@Override
	public void visitDomain(Domain domain) {
		if (m_configManager.validateDomain(domain.getName())) {
			m_currentDomain = domain.getName();
			super.visitDomain(domain);
		}
	}

	@Override
	public void visitError(Error error) {
		ExceptionExclude exceptionExclude = m_exceptionConfigManager.queryExceptionExclude(m_currentDomain, error.getId());

		if (exceptionExclude != null) {
			return;
		}

		int warnLimit = -1;
		int errorLimit = -1;
		int count = error.getCount();
		ExceptionLimit exceptionLimit = m_exceptionConfigManager.queryExceptionLimit(m_currentDomain, error.getId());

		m_totalSegmentException += count;

		if (exceptionLimit != null) {
			warnLimit = exceptionLimit.getWarning();
			errorLimit = exceptionLimit.getError();
		}

		if (errorLimit > 0 & warnLimit > 0 & count >= Math.min(warnLimit, errorLimit)) {

			com.dianping.cat.home.alert.report.entity.Domain domain = m_report.findOrCreateDomain(m_currentDomain);
			domain.setName(m_currentDomain);

			com.dianping.cat.home.alert.report.entity.Exception exception = domain.findOrCreateException(error.getId());

			if (errorLimit > 0 && count >= errorLimit) {
				exception.incErrorNumber();
				domain.incErrorNumber();
			} else if (warnLimit > 0 && count >= warnLimit) {
				exception.incWarnNumber();
				domain.incWarnNumber();
			}
		}
	}

	@Override
	public void visitSegment(Segment segment) {
		m_totalSegmentException = 0;

		super.visitSegment(segment);

		ExceptionLimit exceptionLimit = m_exceptionConfigManager.queryTotalLimitByDomain(m_currentDomain);
		int warnLimit = -1;
		int errorLimit = -1;

		if (exceptionLimit != null) {
			warnLimit = exceptionLimit.getWarning();
			errorLimit = exceptionLimit.getError();
		}

		if (errorLimit > 0 & warnLimit > 0 & m_totalSegmentException >= Math.min(warnLimit, errorLimit)) {
			com.dianping.cat.home.alert.report.entity.Domain domain = m_report.findOrCreateDomain(m_currentDomain);
			com.dianping.cat.home.alert.report.entity.Exception exception = domain
			      .findOrCreateException(TOTAL_EXCEPTION_ALERT);

			if (m_totalSegmentException >= errorLimit) {
				domain.incErrorNumber();
				exception.incErrorNumber();
			} else if (m_totalSegmentException >= warnLimit) {
				domain.incWarnNumber();
				exception.incWarnNumber();
			}
		}
	}

	@Override
	public void visitTopReport(TopReport topReport) {
		super.visitTopReport(topReport);
	}
}
