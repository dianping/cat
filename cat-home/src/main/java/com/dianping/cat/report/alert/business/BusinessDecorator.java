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
package com.dianping.cat.report.alert.business;

import java.util.Calendar;
import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.spi.AlertEntity;
import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.decorator.ProjectDecorator;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;

public class BusinessDecorator extends ProjectDecorator {

	public static final String ID = AlertType.Business.getName();

	@Inject
	private AlertSummaryExecutor m_executor;

	@Override
	public String generateContent(AlertEntity alert) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(alert.getDate());
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		Date alertDate = cal.getTime();

		StringBuilder sb = new StringBuilder();
		sb.append(alert.getContent());
		sb.append(buildContactInfo(alert.getDomain()));

		String summaryContext = m_executor.execute(alert.getDomain(), alertDate);
		if (summaryContext != null) {
			sb.append("<br/>").append(summaryContext);
		}

		return sb.toString();
	}

	@Override
	public String generateTitle(AlertEntity alert) {
		StringBuilder sb = new StringBuilder();
		sb.append("[业务告警] [应用名 ").append(alert.getDomain()).append("]");
		sb.append("[业务指标 ").append(alert.getMetric()).append("]");
		return sb.toString();
	}

	@Override
	public String getId() {
		return ID;
	}

}
