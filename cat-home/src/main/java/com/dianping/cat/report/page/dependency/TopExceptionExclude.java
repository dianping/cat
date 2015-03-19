package com.dianping.cat.report.page.dependency;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.consumer.top.model.entity.Domain;
import com.dianping.cat.consumer.top.model.entity.Error;
import com.dianping.cat.consumer.top.model.entity.Segment;
import com.dianping.cat.consumer.top.model.transform.BaseVisitor;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;

public class TopExceptionExclude extends BaseVisitor {

	private ExceptionRuleConfigManager m_configManager;

	private String m_domain;

	private Segment m_segment;

	private List<String> m_exceptions = new ArrayList<String>();

	public TopExceptionExclude(ExceptionRuleConfigManager configManager) {
		m_configManager = configManager;
	}

	@Override
	public void visitDomain(Domain domain) {
		m_domain = domain.getName();

		super.visitDomain(domain);
	}

	@Override
	public void visitError(Error error) {
		String exception = error.getId();
		boolean isExcluded = m_configManager.isExcluded(m_domain, exception);

		if (isExcluded) {
			m_segment.setError(m_segment.getError() - error.getCount());
			m_exceptions.add(exception);
		} else {
			super.visitError(error);
		}
	}

	@Override
	public void visitSegment(Segment segment) {
		m_segment = segment;
		m_exceptions.clear();

		super.visitSegment(segment);

		for (String exception : m_exceptions) {
			segment.removeError(exception);
		}
	}

}
