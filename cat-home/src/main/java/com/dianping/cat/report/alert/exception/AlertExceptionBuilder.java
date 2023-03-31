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
package com.dianping.cat.report.alert.exception;

import com.dianping.cat.alarm.spi.AlertLevel;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.report.page.dependency.TopMetric.Item;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import java.util.*;
import java.util.Map.Entry;

@Named
public class AlertExceptionBuilder {

	@Inject
	private ExceptionRuleConfigManager m_exceptionConfigManager;

	public Map<String, GroupAlertException> buildAlertExceptions(List<Item> items) {
		Map<String, GroupAlertException> alertExceptions = new LinkedHashMap<>();

		for (Item item : items) {
			GroupAlertException groupAlertException = buildDomainAlertExceptions(item);
			alertExceptions.put(item.getDomain(), groupAlertException);
		}
		return alertExceptions;
	}

	private GroupAlertException buildDomainAlertExceptions(Item item) {
		String domain = item.getDomain();
		List<AlertException> specExceptions = new ArrayList<>();
		List<AlertException> totalExceptions = new ArrayList<>();
		Pair<Double, Double> totalLimitPair = queryDomainTotalLimit(domain);
		double totalException = 0;

		for (Entry<String, Double> entry : item.getException().entrySet()) {
			String exceptionName = entry.getKey();
			double value = entry.getValue().doubleValue();
			Pair<Double, Double> limitPair = queryDomainExceptionLimit(domain, exceptionName);
			totalException += value;

			//非Total告警开关
			if (null != limitPair) {
				double warnLimit = limitPair.getKey();
				double errorLimit = limitPair.getValue();
				if (errorLimit > 0 && value >= errorLimit) {
					specExceptions.add(new AlertException(exceptionName, AlertLevel.ERROR, value));
				} else if (warnLimit > 0 && value >= warnLimit) {
					specExceptions.add(new AlertException(exceptionName, AlertLevel.WARNING, value));
				}
			}
		}

		//Total告警开关
		if (null != totalLimitPair) {
			double totalWarnLimit = totalLimitPair.getKey();
			double totalErrorLimit = totalLimitPair.getValue();
			if (totalErrorLimit > 0 && totalException >= totalErrorLimit) {
				totalExceptions.add(new AlertException(ExceptionRuleConfigManager.TOTAL_STRING, AlertLevel.ERROR, totalException));
				for (Entry<String, Double> entry : item.getException().entrySet()) {
					totalExceptions.add(new AlertException(entry.getKey(), AlertLevel.ERROR, entry.getValue().doubleValue()));
				}
			} else if (totalWarnLimit > 0 && totalException >= totalWarnLimit) {
				totalExceptions.add(new AlertException(ExceptionRuleConfigManager.TOTAL_STRING, AlertLevel.WARNING, totalException));
				for (Entry<String, Double> entry : item.getException().entrySet()) {
					totalExceptions.add(new AlertException(entry.getKey(), AlertLevel.WARNING, entry.getValue().doubleValue()));
				}
			}
		}
		return new GroupAlertException(specExceptions, totalExceptions);
	}

	private Pair<Double, Double> queryDomainExceptionLimit(String domain, String exceptionName) {
		ExceptionLimit exceptionLimit = m_exceptionConfigManager.queryExceptionLimit(domain, exceptionName);
		Pair<Double, Double> limits = new Pair<Double, Double>();
		double warnLimit = -1;
		double errorLimit = -1;

		if (exceptionLimit != null) {
			//告警开关
			if (null != exceptionLimit.getAvailable() && !exceptionLimit.getAvailable()) {
				return null;
			}
			warnLimit = exceptionLimit.getWarning();
			errorLimit = exceptionLimit.getError();
		}
		limits.setKey(warnLimit);
		limits.setValue(errorLimit);

		return limits;
	}

	private Pair<Double, Double> queryDomainTotalLimit(String domain) {
		ExceptionLimit totalExceptionLimit = m_exceptionConfigManager.queryTotalLimitByDomain(domain);
		Pair<Double, Double> limits = new Pair<Double, Double>();
		double totalWarnLimit = -1;
		double totalErrorLimit = -1;

		if (totalExceptionLimit != null) {
			//告警开关
			if (null != totalExceptionLimit.getAvailable() && !totalExceptionLimit.getAvailable()) {
				return null;
			}
			totalWarnLimit = totalExceptionLimit.getWarning();
			totalErrorLimit = totalExceptionLimit.getError();
		}
		limits.setKey(totalWarnLimit);
		limits.setValue(totalErrorLimit);

		return limits;
	}
}
