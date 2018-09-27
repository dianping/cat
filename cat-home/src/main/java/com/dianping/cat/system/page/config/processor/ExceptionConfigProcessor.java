package com.dianping.cat.system.page.config.processor;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.util.StringUtils;

import com.dianping.cat.home.exception.entity.ExceptionExclude;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class ExceptionConfigProcessor {

	@Inject
	private GlobalConfigProcessor m_globalConfigProcessor;

	@Inject
	private ExceptionRuleConfigManager m_exceptionRuleConfigManager;

	private void deleteExceptionExclude(Payload payload) {
		m_exceptionRuleConfigManager.deleteExceptionExclude(payload.getDomain(), payload.getException());
	}

	private void deleteExceptionLimit(Payload payload) {
		m_exceptionRuleConfigManager.deleteExceptionLimit(payload.getDomain(), payload.getException());
	}

	private void loadExceptionConfig(Model model) {
		model.setExceptionExcludes(m_exceptionRuleConfigManager.queryAllExceptionExcludes());
		model.setExceptionLimits(m_exceptionRuleConfigManager.queryAllExceptionLimits());
	}

	public void process(Action action, Payload payload, Model model) {
		switch (action) {
		case EXCEPTION:
			loadExceptionConfig(model);
			break;
		case EXCEPTION_THRESHOLD_DELETE:
			deleteExceptionLimit(payload);
			loadExceptionConfig(model);
			break;
		case EXCEPTION_THRESHOLD_UPDATE:
			model.setExceptionLimit(m_exceptionRuleConfigManager.queryExceptionLimit(payload.getDomain(),
			      payload.getException()));
			break;
		case EXCEPTION_THRESHOLD_ADD:
			List<String> exceptionThresholdList = queryExceptionList();

			exceptionThresholdList.add(ExceptionRuleConfigManager.TOTAL_STRING);
			model.setExceptionList(exceptionThresholdList);
			model.setDomainList(m_globalConfigProcessor.queryDoaminList());
			break;
		case EXCEPTION_THRESHOLD_UPDATE_SUBMIT:
			updateExceptionLimit(payload);
			loadExceptionConfig(model);
			break;
		case EXCEPTION_EXCLUDE_DELETE:
			deleteExceptionExclude(payload);
			loadExceptionConfig(model);
			break;
		case EXCEPTION_EXCLUDE_ADD:
			List<String> exceptionExcludeList = queryExceptionList();

			model.setExceptionList(exceptionExcludeList);
			model.setDomainList(m_globalConfigProcessor.queryDoaminList());
			break;
		case EXCEPTION_EXCLUDE_UPDATE_SUBMIT:
			updateExceptionExclude(payload);
			loadExceptionConfig(model);
			break;
		default:
			throw new RuntimeException("Error action name " + action.getName());
		}
	}

	private List<String> queryExceptionList() {
		return new ArrayList<String>();
	}

	private void updateExceptionExclude(Payload payload) {
		ExceptionExclude exclude = payload.getExceptionExclude();
		exclude.setDomain(exclude.getDomain().trim());
		exclude.setName(exclude.getName().trim());
		exclude.setId(exclude.getDomain() + ":" + exclude.getName());

		if (StringUtils.isNotEmpty(exclude.getDomain()) && StringUtils.isNotEmpty(exclude.getName()))
			m_exceptionRuleConfigManager.insertExceptionExclude(exclude);
	}

	private void updateExceptionLimit(Payload payload) {
		ExceptionLimit limit = payload.getExceptionLimit();
		limit.setDomain(limit.getDomain().trim());
		limit.setName(limit.getName().trim());
		limit.setId(limit.getDomain() + ":" + limit.getName());

		if (StringUtils.isNotEmpty(limit.getDomain()) && StringUtils.isNotEmpty(limit.getName())) {
			m_exceptionRuleConfigManager.insertExceptionLimit(limit);
		}
	}
}
