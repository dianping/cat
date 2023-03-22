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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import com.dianping.cat.alarm.spi.AlertLevel;
import com.dianping.cat.home.exception.entity.ExceptionLimit;
import com.dianping.cat.report.page.dependency.TopMetric.Item;

@Named
public class AlertExceptionBuilder {

	@Inject
	private ExceptionRuleConfigManager m_exceptionConfigManager;

	public Map<String, List<AlertException>> buildAlertExceptions(List<Item> items) {
		Map<String, List<AlertException>> alertExceptions = new LinkedHashMap<String, List<AlertException>>();

		for (Item item : items) {
			List<AlertException> domainAlertExceptions = buildDomainAlertExceptions(item);

			if (!domainAlertExceptions.isEmpty()) {
				alertExceptions.put(item.getDomain(), domainAlertExceptions);
			}
		}
		return alertExceptions;
	}

	private List<AlertException> buildDomainAlertExceptions(Item item) {
		String domain = item.getDomain();
		List<AlertException> alertExceptions = new ArrayList<AlertException>();
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
					alertExceptions.add(new AlertException(exceptionName, AlertLevel.ERROR, value));
				} else if (warnLimit > 0 && value >= warnLimit) {
					alertExceptions.add(new AlertException(exceptionName, AlertLevel.WARNING, value));
				}
			}
		}

		//Total告警开关
		if (null != totalLimitPair) {
			double totalWarnLimit = totalLimitPair.getKey();
			double totalErrorLimit = totalLimitPair.getValue();
			if (totalErrorLimit > 0 && totalException >= totalErrorLimit) {
				alertExceptions.add(new AlertException(ExceptionRuleConfigManager.TOTAL_STRING, AlertLevel.ERROR, totalException));
			} else if (totalWarnLimit > 0 && totalException >= totalWarnLimit) {
				alertExceptions.add(new AlertException(ExceptionRuleConfigManager.TOTAL_STRING, AlertLevel.WARNING, totalException));
			}
		}
		return alertExceptions;
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

	public class AlertException {

		private String m_name;

		private AlertLevel m_type;

		private double m_count;

		public AlertException(String name, AlertLevel type, double count) {
			m_name = name;
			m_type = type;
			m_count = count;
		}

		public String getName() {
			return m_name;
		}

		public AlertLevel getType() {
			return m_type;
		}

		@Override
		public String toString() {
			return "[ 异常名称: " + m_name + " 异常数量：" + m_count + " ]";
		}
	}

}
