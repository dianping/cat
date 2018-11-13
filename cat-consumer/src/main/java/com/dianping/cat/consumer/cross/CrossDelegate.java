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
package com.dianping.cat.consumer.cross;

import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.cross.model.entity.CrossReport;
import com.dianping.cat.consumer.cross.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.cross.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.cross.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.Date;
import java.util.Map;

@Named(type = ReportDelegate.class, value = CrossAnalyzer.ID)
public class CrossDelegate implements ReportDelegate<CrossReport> {

	@Inject
	private TaskManager m_taskManager;

	@Inject
	private ServerFilterConfigManager m_serverFilterConfigManager;

	@Override
	public void afterLoad(Map<String, CrossReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, CrossReport> reports) {
	}

	@Override
	public byte[] buildBinary(CrossReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(CrossReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(CrossReport report) {
		String domain = report.getDomain();

		if (m_serverFilterConfigManager.validateDomain(domain)) {
			return m_taskManager.createTask(report.getStartTime(), domain, CrossAnalyzer.ID,	TaskProlicy.ALL_EXCLUED_HOURLY);
		} else {
			return true;
		}
	}

	@Override
	public String getDomain(CrossReport report) {
		return report.getDomain();
	}

	@Override
	public CrossReport makeReport(String domain, long startTime, long duration) {
		CrossReport report = new CrossReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public CrossReport mergeReport(CrossReport old, CrossReport other) {
		CrossReportMerger merger = new CrossReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public CrossReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public CrossReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}
}
