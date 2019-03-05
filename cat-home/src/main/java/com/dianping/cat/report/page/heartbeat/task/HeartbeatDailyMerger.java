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
package com.dianping.cat.report.page.heartbeat.task;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Machine;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultMerger;
import com.dianping.cat.helper.TimeHelper;

public class HeartbeatDailyMerger extends DefaultMerger {

	private long m_currentDay;

	private int m_hourIndex;

	public HeartbeatDailyMerger(HeartbeatReport heartbeatReport, long currentDay) {
		super(heartbeatReport);

		m_currentDay = currentDay;
	}

	@Override
	public void visitHeartbeatReport(HeartbeatReport from) {
		long start = from.getStartTime().getTime();
		m_hourIndex = (int) ((start - m_currentDay) / TimeHelper.ONE_HOUR);

		super.visitHeartbeatReport(from);
	}

	@Override
	protected void visitMachineChildren(Machine to, Machine from) {
		for (Period source : from.getPeriods()) {
			int minute = source.getMinute();

			source.setMinute(60 * m_hourIndex + minute);
		}
		super.visitMachineChildren(to, from);
	}
}
