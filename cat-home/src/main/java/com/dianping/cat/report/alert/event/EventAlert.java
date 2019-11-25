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
package com.dianping.cat.report.alert.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;
import org.unidal.tuple.Pair;

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
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.model.entity.EventName;
import com.dianping.cat.consumer.event.model.entity.EventReport;
import com.dianping.cat.consumer.event.model.entity.EventType;
import com.dianping.cat.consumer.event.model.entity.Range;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.event.transform.EventMergeHelper;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Named
public class EventAlert implements Task, LogEnabled {

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	private static final int DATA_ALREADY_MINUTE = 1;

	private static String MIN = "min";

	private static String MAX = "max";

	private static String COUNT = "count";

	private static String FAIL_RATIO = "failRatio";

	@Inject
	protected EventRuleConfigManager m_ruleConfigManager;

	@Inject
	protected DataChecker m_dataChecker;

	@Inject
	protected AlertManager m_sendManager;

	protected Logger m_logger;

	@Inject(type = ModelService.class, value = EventAnalyzer.ID)
	private ModelService<EventReport> m_service;

	@Inject
	private EventMergeHelper m_mergeHelper;

	private double[] buildArrayData(int start, int end, String type, String name, String monitor, EventReport report) {
		EventType t = report.findOrCreateMachine(Constants.ALL).findOrCreateType(type);
		EventName eventName = t.findOrCreateName(name);
		Map<Integer, Range> range = eventName.getRanges();
		int length = end - start + 1;
		double[] datas = new double[60];
		double[] result = new double[length];

		if (COUNT.equalsIgnoreCase(monitor)) {
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
		}
		System.arraycopy(datas, start, result, 0, length);

		return result;
	}

	protected int calAlreadyMinute() {
		long current = (System.currentTimeMillis()) / 1000 / 60;
		int minute = (int) (current % (60)) - DATA_ALREADY_MINUTE;

		return minute;
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

				pars.put(MIN, String.valueOf(start));
				pars.put(MAX, String.valueOf(end));

				EventReport report = fetchEventReport(domain, ModelPeriod.CURRENT, pars);

				if (report != null) {
					double[] data = buildArrayData(start, end, type, name, monitor, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else if (minute < 0) {
				int start = 60 + minute + 1 - (maxMinute);
				int end = 60 + minute;

				pars.put(MIN, String.valueOf(start));
				pars.put(MAX, String.valueOf(end));

				EventReport report = fetchEventReport(domain, ModelPeriod.LAST, pars);

				if (report != null) {
					double[] data = buildArrayData(start, end, type, name, monitor, report);

					results.addAll(m_dataChecker.checkData(data, conditions));
				}
			} else {
				int currentStart = 0, currentEnd = minute;
				int lastStart = 60 + 1 - (maxMinute - minute);
				int lastEnd = 59;

				pars.put(MIN, String.valueOf(currentStart));
				pars.put(MAX, String.valueOf(currentEnd));

				EventReport currentReport = fetchEventReport(domain, ModelPeriod.CURRENT, pars);

				pars.put(MIN, String.valueOf(lastStart));
				pars.put(MAX, String.valueOf(lastEnd));

				EventReport lastReport = fetchEventReport(domain, ModelPeriod.LAST, pars);

				if (currentReport != null && lastReport != null) {
					double[] currentValue = buildArrayData(currentStart, currentEnd, type, name, monitor, currentReport);

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

	private EventReport fetchEventReport(String domain, ModelPeriod period, Map<String, String> pars) {
		ModelRequest request = new ModelRequest(domain, period.getStartTime()).setProperty("ip", Constants.ALL)
								.setProperty("requireAll", "true");

		request.getProperties().putAll(pars);

		ModelResponse<EventReport> response = m_service.invoke(request);

		if (response != null) {
			EventReport report = response.getModel();

			return m_mergeHelper.mergeAllNames(report, Constants.ALL, pars.get("name"));
		} else {
			return null;
		}
	}

	@Override
	public String getName() {
		return AlertType.Event.getName();
	}

	protected double[] mergerArray(double[] from, double[] to) {
		int fromLength = from.length;
		int toLength = to.length;
		double[] result = new double[fromLength + toLength];
		int index = 0;

		for (int i = 0; i < fromLength; i++) {
			result[i] = from[i];
			index++;
		}
		for (int i = 0; i < toLength; i++) {
			result[i + index] = to[i];
		}
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
			Transaction t = Cat.newTransaction("AlertEvent", TimeHelper.getMinuteStr());
			long current = System.currentTimeMillis();

			try {
				MonitorRules monitorRules = m_ruleConfigManager.getMonitorRules();
				Map<String, Rule> rules = monitorRules.getRules();

				for (Entry<String, Rule> entry : rules.entrySet()) {
					//Event告警开关
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
