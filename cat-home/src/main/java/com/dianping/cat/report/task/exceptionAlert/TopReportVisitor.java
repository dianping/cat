package com.dianping.cat.report.task.exceptionAlert;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.consumer.top.model.transform.BaseVisitor;
import com.dianping.cat.home.alertReport.entity.AlertReport;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.system.config.ExceptionThresholdConfigManager;

public class TopReportVisitor extends BaseVisitor {

	private ExceptionThresholdConfigManager m_configManager;

	private AlertReport m_report;

	private String m_currentDomain;

	private static final String TOTAL_EXCEPTION_ALERT = "TotalExceptionAlert";

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
		int warnLimit = -1;
		int errorLimit = -1;

		ExceptionLimit exceptionLimit = m_configManager.queryDomainExceptionLimit(m_currentDomain, error.getId());

		if (exceptionLimit != null) {
			warnLimit = exceptionLimit.getWarning();
			errorLimit = exceptionLimit.getError();
		}
		int count = error.getCount();

		if (errorLimit > 0 & warnLimit > 0 & count >= Math.min(warnLimit, errorLimit)) {

			com.dianping.cat.home.alertReport.entity.Domain domain = m_report.findOrCreateDomain(m_currentDomain);
			domain.setName(m_currentDomain);

			com.dianping.cat.home.alertReport.entity.Exception exception = domain.findOrCreateException(error.getId());

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
		long totalSegmentException = segment.getError();
		ExceptionLimit exceptionLimit = m_configManager.queryDomainTotalLimit(m_currentDomain);
		int warnLimit = -1;
		int errorLimit = -1;
		
		if (exceptionLimit != null) {
			warnLimit = exceptionLimit.getWarning();
			errorLimit = exceptionLimit.getError();
		}

		if (errorLimit > 0 & warnLimit > 0 & totalSegmentException >= Math.min(warnLimit, errorLimit)) {
			com.dianping.cat.home.alertReport.entity.Domain domain = m_report.findOrCreateDomain(m_currentDomain);
			com.dianping.cat.home.alertReport.entity.Exception exception = domain
			      .findOrCreateException(TOTAL_EXCEPTION_ALERT);

			if (totalSegmentException >= errorLimit) {
				domain.incErrorNumber();
				exception.incErrorNumber();
			} else if (totalSegmentException >= warnLimit) {
				domain.incWarnNumber();
				exception.incWarnNumber();
			}

		}
		super.visitSegment(segment);
	}

	@Override
	public void visitTopReport(TopReport topReport) {
		super.visitTopReport(topReport);
	}
}
