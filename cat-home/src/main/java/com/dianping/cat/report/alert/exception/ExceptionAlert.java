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

import com.dianping.cat.Cat;
import com.dianping.cat.Constants;
import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertManager;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.model.entity.TopReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.report.page.dependency.TopExceptionExclude;
import com.dianping.cat.report.page.dependency.TopMetric;
import com.dianping.cat.report.page.dependency.TopMetric.Item;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import org.apache.commons.collections.CollectionUtils;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.*;
import java.util.Map.Entry;

@Named
public class ExceptionAlert implements Task {

	protected static final long DURATION = TimeHelper.ONE_MINUTE;

	protected static final int ALERT_PERIOD = 1;

	@Inject
	protected ExceptionRuleConfigManager m_exceptionConfigManager;

	@Inject
	protected AlertExceptionBuilder m_alertBuilder;

	@Inject(type = ModelService.class, value = TopAnalyzer.ID)
	protected ModelService<TopReport> m_topService;

	@Inject
	protected AlertManager m_sendManager;

	protected TopMetric buildTopMetric(Date date) {
		TopReport topReport = queryTopReport(date);
		TopMetric topMetric = new TopMetric(ALERT_PERIOD, Integer.MAX_VALUE, m_exceptionConfigManager);

		topMetric.setStart(date).setEnd(new Date(date.getTime() + TimeHelper.ONE_MINUTE - 1));
		topMetric.visitTopReport(topReport);
		return topMetric;
	}

	public String getName() {
		return AlertType.Exception.getName();
	}

	private void handleExceptions(List<Item> itemList) {
		Map<String, GroupAlertException> alertExceptions = m_alertBuilder.buildAlertExceptions(itemList);

		// 告警开关
		if (alertExceptions.isEmpty()) {
			return;
		}

		StringBuilder alertContent = new StringBuilder();
		StringBuilder specContent = new StringBuilder();
		for (Entry<String, GroupAlertException> entry : alertExceptions.entrySet()) {
			try {
				String domain = entry.getKey();
				GroupAlertException exceptions = entry.getValue();
				if (CollectionUtils.isNotEmpty(exceptions.getTotalExceptions())) {
					AlertException topException = exceptions.getTotalExceptions().get(0);
					AlertEntity entity = new AlertEntity();
					entity.setGroup(domain);
					entity.setDate(new Date());
					entity.setType(getName());
					entity.setMetric(topException.getName());
					entity.setLevel(topException.getType());
					alertContent.append("当前值=").append(topException.showCount()).append("，阈值=");
					switch (topException.getType()) {
						case WARNING:
							alertContent.append(exceptions.showTotalWarnLimit());
							break;
						case ERROR:
							alertContent.append(exceptions.showTotalErrorLimit());
							break;
					}
					alertContent.append("<br/>错误分布：");
					for (AlertMachine machine : exceptions.getTotalMachines()) {
						alertContent.append("<br/>").append(machine.toString());
					}
					alertContent.append("<br/>错误信息：");
					int i = 0;
					for (AlertException exception : exceptions.getTotalExceptions()) {
						if (i > 0) {
							alertContent.append("<br/>").append(exception.toString());
						}
						i++;
					}

					entity.setContent(alertContent.toString());
					alertContent.setLength(0);
					m_sendManager.addAlert(entity);
				}

				if (CollectionUtils.isNotEmpty(exceptions.getSpecExceptions())) {
					for (AlertException exception : exceptions.getSpecExceptions()) {
						AlertEntity entity = new AlertEntity();
						entity.setGroup(domain);
						entity.setDate(new Date());
						entity.setType(getName());
						entity.setMetric(exception.getName());
						entity.setLevel(exception.getType());

						specContent.append(exception.toString())
							.append("当前值=").append(exception.showCount()).append("，阈值=");
						switch (exception.getType()) {
							case WARNING:
								specContent.append(exceptions.showSpecWarnLimit());
								break;
							case ERROR:
								specContent.append(exceptions.showSpecErrorLimit());
								break;
						}
						specContent.append("<br/>错误分布：");
						for (AlertMachine machine : exceptions.getSpecMachines()) {
							specContent.append("<br/>").append(machine.toString());
						}
						entity.setContent(specContent.toString());
						specContent.setLength(0);
						m_sendManager.addAlert(entity);
					}
				}

			} catch (Exception e) {
				Cat.logError(e);
			}
		}
	}

	protected TopReport queryTopReport(Date start) {
		String domain = Constants.CAT;
		String date = String.valueOf(start.getTime());
		ModelRequest request = new ModelRequest(domain, start.getTime()).setProperty("date", date);

		if (m_topService.isEligable(request)) {
			ModelResponse<TopReport> response = m_topService.invoke(request);
			TopReport report = response.getModel();

			report.accept(new TopExceptionExclude(m_exceptionConfigManager));
			return report;
		} else {
			throw new RuntimeException("Internal error: no eligable top service registered for " + request + "!");
		}
	}

	@Override
	public void run() {
		boolean active = TimeHelper.sleepToNextMinute();

		while (active) {
			long current = System.currentTimeMillis();
			Transaction t = Cat.newTransaction("AlertException", TimeHelper.getMinuteStr());

			try {
				TopMetric topMetric = buildTopMetric(new Date(current - TimeHelper.ONE_MINUTE - current%TimeHelper.ONE_MINUTE));
				Collection<List<Item>> itemLists = topMetric.getError().getResult().values();
				List<Item> itemList = new ArrayList<Item>();

				if (!itemLists.isEmpty()) {
					itemList = itemLists.iterator().next();
				}
				List<Item> items = new ArrayList<Item>();

				for (Item item : itemList) {
					if (!Constants.FRONT_END.equals(item.getDomain())) {
						items.add(item);
					}
				}
				handleExceptions(items);

				t.setStatus(Transaction.SUCCESS);
			} catch (Exception e) {
				t.setStatus(e);
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
