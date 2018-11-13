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
package com.dianping.cat.consumer.dependency;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.dependency.model.entity.DependencyReport;
import com.dianping.cat.consumer.dependency.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.dependency.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.dependency.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.Date;
import java.util.Map;

@Named(type = ReportDelegate.class, value = DependencyAnalyzer.ID)
public class DependencyDelegate implements ReportDelegate<DependencyReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, DependencyReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, DependencyReport> reports) {
	}

	@Override
	public byte[] buildBinary(DependencyReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(DependencyReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(DependencyReport report) {
		return m_taskManager.createTask(report.getStartTime(), Constants.CAT, DependencyAnalyzer.ID, TaskProlicy.HOULY);
	}

	@Override
	public String getDomain(DependencyReport report) {
		return report.getDomain();
	}

	@Override
	public DependencyReport makeReport(String domain, long startTime, long duration) {
		DependencyReport report = new DependencyReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public DependencyReport mergeReport(DependencyReport old, DependencyReport other) {
		DependencyReportMerger merger = new DependencyReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public DependencyReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public DependencyReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}
}
