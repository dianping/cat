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
package com.dianping.cat.consumer.matrix;

import java.util.Date;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.consumer.matrix.model.entity.MatrixReport;
import com.dianping.cat.consumer.matrix.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.matrix.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.matrix.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

@Named(type = ReportDelegate.class, value = MatrixAnalyzer.ID)
public class MatrixDelegate implements ReportDelegate<MatrixReport> {

	@Inject
	private TaskManager m_taskManager;

	@Inject
	private ServerFilterConfigManager m_configManager;

	@Override
	public void afterLoad(Map<String, MatrixReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, MatrixReport> reports) {
	}

	@Override
	public byte[] buildBinary(MatrixReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public String buildXml(MatrixReport report) {
		return report.toString();
	}

	@Override
	public boolean createHourlyTask(MatrixReport report) {
		String domain = report.getDomain();

		if (m_configManager.validateDomain(domain)) {
			return m_taskManager.createTask(report.getStartTime(), domain, MatrixAnalyzer.ID,	TaskProlicy.ALL_EXCLUED_HOURLY);
		} else {
			return true;
		}
	}

	@Override
	public String getDomain(MatrixReport report) {
		return report.getDomain();
	}

	@Override
	public MatrixReport makeReport(String domain, long startTime, long duration) {
		MatrixReport report = new MatrixReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public MatrixReport mergeReport(MatrixReport old, MatrixReport other) {
		MatrixReportMerger merger = new MatrixReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public MatrixReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public MatrixReport parseXml(String xml) throws Exception {
		MatrixReport report = DefaultSaxParser.parse(xml);

		return report;
	}
}
