/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

		List<ExceptionLimit> exceptionLimits = m_exceptionRuleConfigManager
				.queryAllExceptionLimits();
		rulesAvailableBuild(exceptionLimits);
		model.setExceptionLimits(exceptionLimits);
	}

	//增加告警开关功能，但是线上并无available值，这里做一个兼容
	private void rulesAvailableBuild(List<ExceptionLimit> exceptionLimits) {
		if (exceptionLimits == null || exceptionLimits.isEmpty()) {
			return;
		}
		for (ExceptionLimit exceptionLimit : exceptionLimits) {
			if (null == exceptionLimit.getAvailable()) {
				exceptionLimit.setAvailable(true);
			}
		}
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
			model.setExceptionLimit(
									m_exceptionRuleConfigManager.queryExceptionLimit(payload.getDomain(),	payload.getException()));
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
		limit.setAvailable(limit.getAvailable());

		if (StringUtils.isNotEmpty(limit.getDomain()) && StringUtils.isNotEmpty(limit.getName())) {
			m_exceptionRuleConfigManager.insertExceptionLimit(limit);
		}
	}
}
