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
package com.dianping.cat.consumer.business;

import java.util.Date;
import java.util.Map;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.consumer.business.model.entity.BusinessReport;
import com.dianping.cat.consumer.business.model.transform.DefaultNativeBuilder;
import com.dianping.cat.consumer.business.model.transform.DefaultNativeParser;
import com.dianping.cat.consumer.business.model.transform.DefaultSaxParser;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.task.TaskManager;
import com.dianping.cat.task.TaskManager.TaskProlicy;

@Named(type = ReportDelegate.class, value = BusinessAnalyzer.ID)
public class BusinessDelegate implements ReportDelegate<BusinessReport> {

	@Inject
	private TaskManager m_taskManager;

	@Override
	public void afterLoad(Map<String, BusinessReport> reports) {
	}

	@Override
	public void beforeSave(Map<String, BusinessReport> reports) {
	}

	@Override
	public byte[] buildBinary(BusinessReport report) {
		return DefaultNativeBuilder.build(report);
	}

	@Override
	public BusinessReport parseBinary(byte[] bytes) {
		return DefaultNativeParser.parse(bytes);
	}

	@Override
	public String buildXml(BusinessReport report) {
		return report.toString();
	}

	@Override
	public String getDomain(BusinessReport report) {
		return report.getDomain();
	}

	@Override
	public BusinessReport makeReport(String domain, long startTime, long duration) {
		BusinessReport report = new BusinessReport(domain);

		report.setStartTime(new Date(startTime));
		report.setEndTime(new Date(startTime + duration - 1));

		return report;
	}

	@Override
	public BusinessReport mergeReport(BusinessReport old, BusinessReport other) {
		BusinessReportMerger merger = new BusinessReportMerger(old);

		other.accept(merger);
		return old;
	}

	@Override
	public BusinessReport parseXml(String xml) throws Exception {
		return DefaultSaxParser.parse(xml);
	}

	@Override
	public boolean createHourlyTask(BusinessReport report) {
		return m_taskManager.createTask(report.getStartTime(), report.getDomain(), BusinessAnalyzer.ID, TaskProlicy.DAILY);
	}

}
