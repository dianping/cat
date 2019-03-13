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
package com.dianping.cat.consumer.heartbeat;

import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.entity.Period;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultMerger;

public class HeartbeatReportMerger extends DefaultMerger {
	public HeartbeatReportMerger(HeartbeatReport heartbeatReport) {
		super(heartbeatReport);
	}

	@Override
	protected void mergePeriod(Period old, Period period) {
		old.setCatMessageOverflow(period.getCatMessageOverflow());
		old.setCatMessageProduced(period.getCatMessageProduced());
		old.setCatMessageSize(period.getCatMessageSize());
		old.setCatThreadCount(period.getCatThreadCount());
		old.setDaemonCount(period.getDaemonCount());
		old.setHeapUsage(period.getHeapUsage());
		old.setHttpThreadCount(period.getHttpThreadCount());
		old.setMemoryFree(period.getMemoryFree());
		old.setMinute(period.getMinute());
		old.setNewGcCount(period.getNewGcCount());
		old.setNoneHeapUsage(period.getNoneHeapUsage());
		old.setOldGcCount(period.getOldGcCount());
		old.setPigeonThreadCount(period.getPigeonThreadCount());
		old.setSystemLoadAverage(period.getSystemLoadAverage());
		old.setThreadCount(period.getThreadCount());
		old.setTotalStartedCount(period.getTotalStartedCount());
	}

	@Override
	public void visitHeartbeatReport(HeartbeatReport heartbeatReport) {
		super.visitHeartbeatReport(heartbeatReport);

		getHeartbeatReport().getIps().addAll(heartbeatReport.getIps());
	}

}
