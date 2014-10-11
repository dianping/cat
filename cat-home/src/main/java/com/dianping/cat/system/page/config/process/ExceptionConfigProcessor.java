package com.dianping.cat.system.page.config.process;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.home.bug.entity.BugReport;
import com.dianping.cat.home.dependency.exception.entity.ExceptionExclude;
import com.dianping.cat.home.dependency.exception.entity.ExceptionLimit;
import com.dianping.cat.report.service.ReportServiceManager;
import com.dianping.cat.system.config.ExceptionConfigManager;
import com.dianping.cat.system.page.config.Action;
import com.dianping.cat.system.page.config.Model;
import com.dianping.cat.system.page.config.Payload;

public class ExceptionConfigProcessor {

	@Inject
	private GlobalConfigProcessor m_globalConfigProcessor;

	@Inject
	private ExceptionConfigManager m_exceptionConfigManager;

	@Inject
	private ReportServiceManager m_reportService;

	private void deleteExceptionExclude(Payload payload) {
		m_exceptionConfigManager.deleteExceptionExclude(payload.getDomain(), payload.getException());
	}

	private void deleteExceptionLimit(Payload payload) {
		m_exceptionConfigManager.deleteExceptionLimit(payload.getDomain(), payload.getException());
	}

	private void loadExceptionConfig(Model model) {
		model.setExceptionExcludes(m_exceptionConfigManager.queryAllExceptionExcludes());
		model.setExceptionLimits(m_exceptionConfigManager.queryAllExceptionLimits());
	}

	public void process(Action action,Payload payload,Model model){
		switch(action){
		case EXCEPTION:
			loadExceptionConfig(model);
			break;
		case EXCEPTION_THRESHOLD_DELETE:
			deleteExceptionLimit(payload);
			loadExceptionConfig(model);
			break;
		case EXCEPTION_THRESHOLD_UPDATE:
			model.setExceptionLimit(m_exceptionConfigManager.queryDomainExceptionLimit(payload.getDomain(),
			      payload.getException()));
			break;
		case EXCEPTION_THRESHOLD_ADD:
			List<String> exceptionThresholdList = queryExceptionList();

			exceptionThresholdList.add(ExceptionConfigManager.TOTAL_STRING);
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
		case EXCEPTION_EXCLUDE_UPDATE:
			model.setExceptionExclude(m_exceptionConfigManager.queryDomainExceptionExclude(payload.getDomain(),
			      payload.getException()));
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
	}	private List<String> queryExceptionList() {
		long current = System.currentTimeMillis();
		Date start = new Date(current - current % TimeHelper.ONE_HOUR - TimeHelper.ONE_HOUR - TimeHelper.ONE_DAY);
		Date end = new Date(start.getTime() + TimeHelper.ONE_HOUR);
		BugReport report = m_reportService.queryBugReport(Constants.CAT, start, end);
		Set<String> keys = new HashSet<String>();
		List<String> exceptions = new ArrayList<String>();

		for (Entry<String, com.dianping.cat.home.bug.entity.Domain> domain : report.getDomains().entrySet()) {
			keys.addAll(domain.getValue().getExceptionItems().keySet());
		}

		for (String key : keys) {
			exceptions.add(key.replaceAll("\n", " "));
		}
		return exceptions;
	}
	private void updateExceptionExclude(Payload payload) {
		ExceptionExclude exclude = payload.getExceptionExclude();
		String domain = payload.getDomain();
		String exception = payload.getException();

		if (domain != null && exception != null) {
			m_exceptionConfigManager.deleteExceptionExclude(domain, exception);
		}
		m_exceptionConfigManager.insertExceptionExclude(exclude);
	}

	private void updateExceptionLimit(Payload payload) {
		ExceptionLimit limit = payload.getExceptionLimit();
		m_exceptionConfigManager.insertExceptionLimit(limit);
	}
}
