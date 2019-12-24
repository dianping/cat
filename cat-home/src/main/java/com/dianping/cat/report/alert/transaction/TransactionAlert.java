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
package com.dianping.cat.report.alert.transaction;

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.alarm.rule.entity.Condition;
import com.dianping.cat.alarm.rule.entity.Config;
import com.dianping.cat.alarm.rule.entity.MonitorRules;
import com.dianping.cat.alarm.rule.entity.Rule;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.rule.DataCheckEntity;
import com.dianping.cat.alarm.spi.rule.DataChecker;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.model.entity.Range;
import com.dianping.cat.consumer.transaction.model.entity.TransactionName;
import com.dianping.cat.consumer.transaction.model.entity.TransactionReport;
import com.dianping.cat.consumer.transaction.model.entity.TransactionType;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Named
public class TransactionAlert implements Task, LogEnabled {

	private static final long DURATION = TimeHelper.ONE_MINUTE;

	private static final int DATA_ALREADY_MINUTE = 1;

	private static final String MAX = "max";

	private static final String AVG = "avg";

	private static final String COUNT = "count";

	private static final String FAIL_RATIO = "failRatio";

	@Inject
	protected TransactionRuleConfigManager m_ruleConfigManager;

	@Inject
	protected DataChecker m_dataChecker;

	@Inject
	protected AlertManager m_sendManager;

	protected Logger m_logger;

	@Inject(type = ModelService.class, value = TransactionAnalyzer.ID)
	private ModelService<TransactionReport> m_service;

	@Inject
	private TransactionMergeHelper m_mergeHelper;

	private double[] buildArrayData(int start, int end, String type, String name, String monitor,
	      TransactionReport report) {
		TransactionType t = report.findOrCreateMachine(Constants.ALL).findOrCreateType(type);
		TransactionName transactionName = t.findOrCreateName(name);
		Map<Integer, Range> range = transactionName.getRanges();
		int length = end - start + 1;
		double[] datas = new double[60];
		double[] result = new double[length];

		if (AVG.equalsIgnoreCase(monitor)) {
			for (Entry<Integer, Range> entry : range.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getAvg();
			}
		} else if (COUNT.equalsIgnoreCase(monitor)) {
			for (Entry<Integer, Range> entry : range.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getCount();
			}
		} else if (FAIL_RATIO.equalsIgnoreCase(monitor)) {
			for (Entry<Integer, Range> entry : range.entrySet()) {
				Range value = entry.getValue();

				if (value.getCount() > 0) {
					datas[entry.getKey()] = value.getFails() * 1.0 / value.getCount();
				}
			}
		} else if (MAX.equalsIgnoreCase(monitor)) {
			for (Entry<Integer, Range> entry : range.entrySet()) {
				datas[entry.getKey()] = entry.getValue().getMax();
			}
		}
		System.arraycopy(datas, start, result, 0, length);

		return result;
	}

	private int calAlreadyMinute() {
		long current = (System.currentTimeMillis()) / 1000 / 60;

		return (int) (current % (60)) - DATA_ALREADY_MINUTE;
	}

	private List<DataCheckEntity> computeAlertForRule(String domain, String type, String name, String monitor,
	      List<Config> configs) {
		List<DataCheckEntity> results = new ArrayList<DataCheckEntity>();
		Pair<Integer, List<Condition>> conditionPair = m_ruleConfigManager.convertConditions(configs);
		int minute = calAlreadyMinute();
		Map<String, String> pars = new HashMap<String, String>();

		pars.put("type", type);
		pars.put("name", name);

		if (conditionPair != null) {
			int maxMinute = conditionPair.getKey();
			List<Condition> conditions = conditionPair.getValue();

			if (StringUtils.isEmpty(name)) {
				name = Constants.ALL;
			}
			if (minute >= maxMinute - 1) {
				int start = minute + 1 - maxMinute;
				int end = minute;

				pars.put("min", String.valueOf(start));
				pars.put("max", String.valueOf(end));

				TransactionReport report = fetchTransactionReport(domain, ModelPeriod.CURRENT, pars);

				if (report != null) {
					double[] data = buildArrayData(start, end, type, name, monitor, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else if (minute < 0) {
				int start = 60 + minute + 1 - (maxMinute);
				int end = 60 + minute;

				pars.put("min", String.valueOf(start));
				pars.put("max", String.valueOf(end));

				TransactionReport report = fetchTransactionReport(domain, ModelPeriod.LAST, pars);

				if (report != null) {
					double[] data = buildArrayData(start, end, type, name, monitor, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else {
				int currentStart = 0;
				int lastStart = 60 + 1 - (maxMinute - minute);
				int lastEnd = 59;

				pars.put("min", String.valueOf(currentStart));
				pars.put("max", String.valueOf(minute));

				TransactionReport currentReport = fetchTransactionReport(domain, ModelPeriod.CURRENT, pars);

				pars.put("min", String.valueOf(lastStart));
				pars.put("max", String.valueOf(lastEnd));

				TransactionReport lastReport = fetchTransactionReport(domain, ModelPeriod.LAST, pars);

				if (currentReport != null && lastReport != null) {
					double[] currentValue = buildArrayData(currentStart, minute, type, name, monitor, currentReport);

					double[] lastValue = buildArrayData(lastStart, lastEnd, type, name, monitor, lastReport);

					double[] data = mergerArray(lastValue, currentValue);
					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			}
		}
		return results;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	private TransactionReport fetchTransactionReport(String domain, ModelPeriod period, Map<String, String> pars) {
		ModelRequest request = new ModelRequest(domain, period.getStartTime()).setProperty("ip", Constants.ALL)
		      .setProperty("requireAll", "true");

		request.getProperties().putAll(pars);

		ModelResponse<TransactionReport> response = m_service.invoke(request);

		if (response != null) {
			TransactionReport report = response.getModel();

			return m_mergeHelper.mergeAllNames(report, Constants.ALL, pars.get("name"));
		} else {
			return null;
		}
	}

	@Override
	public String getName() {
		return AlertType.Transaction.getName();
	}

	private double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		System.arraycopy(to, 0, result, index, toLength);
		return result;
	}

	private void processRule(Rule rule) {
		List<String> fields = Splitters.by(";").split(rule.getId());
		String domain = fields.get(0);
		String type = fields.get(1);
		String name = fields.get(2);
		String monitor = fields.get(3);

		List<DataCheckEntity> alertResults = computeAlertForRule(domain, type, name, monitor, rule.getConfigs());
		for (DataCheckEntity alertResult : alertResults) {
			AlertEntity entity = new AlertEntity();

			entity.setDate(alertResult.getAlertTime()).setContent(alertResult.getContent())
			      .setLevel(alertResult.getAlertLevel());
			entity.setMetric(type + "-" + name + "-" + monitor).setType(getName()).setGroup(domain);
			m_sendManager.addAlert(entity);
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			Transaction t = Cat.newTransaction("AlertTransaction", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				MonitorRules monitorRules = m_ruleConfigManager.getMonitorRules();
				Map<String, Rule> rules = monitorRules.getRules();

				for (Entry<String, Rule> entry : rules.entrySet()) {
					//告警开关
					if (null != entry.getValue().getAvailable() && !entry.getValue().getAvailable()) {
						continue;
					}
					try {
						processRule(entry.getValue());
					} catch (Exception e) {
						Cat.logError(e);
					}
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
				Cat.logError(e);
			} finally {
				t.complete();
			}
			long duration = System.currentTimeMillis() - current;

			try {
				if (duration < DURATION) {
					Thread.sleep(DURATION - duration);
				}
			} catch (InterruptedException e) {
				active = false;
			}
		}
	}

	@Override
	public void shutdown() {
	}

}
