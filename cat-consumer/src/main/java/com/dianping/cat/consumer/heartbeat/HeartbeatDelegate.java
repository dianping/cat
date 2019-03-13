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

import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.heartbeat.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.Date;
import java.util.Map;

@Named(type = ReportDelegate.class, value = HeartbeatAnalyzer.ID)
public class HeartbeatDelegate implements ReportDelegate<HeartbeatReport> {

	@Inject
	private TaskManager m_taskManager;

	@Inject
	private ServerFilterConfigManager m_manager;

	@Override
	public void afterLoad(Map<String, HeartbeatReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, HeartbeatReport> reports) {
	}

	@Override
	public byte[] buildBinary(HeartbeatReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(HeartbeatReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(HeartbeatReport report) {
		String domain = report.getDomain();

		if (m_manager.validateDomain(domain)) {
			return m_taskManager.createTask(report.getStartTime(), domain, HeartbeatAnalyzer.ID, TaskProlicy.DAILY);
		} else {
			return true;
		}
	}

	@Override
	public String getDomain(HeartbeatReport report) {
		return report.getDomain();
	}

	@Override
	public HeartbeatReport makeReport(String domain, long startTime, long duration) {
		HeartbeatReport report = new HeartbeatReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public HeartbeatReport mergeReport(HeartbeatReport old, HeartbeatReport other) {
		HeartbeatReportMerger merger = new HeartbeatReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public HeartbeatReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public HeartbeatReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}
}
