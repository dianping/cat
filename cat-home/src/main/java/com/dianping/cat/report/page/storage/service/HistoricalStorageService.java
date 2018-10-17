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
package com.dianping.cat.report.page.storage.service;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.report.page.storage.task.StorageReportService;
import com.dianping.cat.report.service.BaseHistoricalModelService;
import com.dianping.cat.report.service.ModelRequest;

public class HistoricalStorageService extends BaseHistoricalModelService<StorageReport> {

	@Inject
	private StorageReportService m_reportService;

	public HistoricalStorageService() {
		super(StorageAnalyzer.ID);
	}

	@Override
	protected StorageReport buildModel(ModelRequest request) throws Exception {
		String domain = request.getDomain();
		long date = request.getStartTime();
		StorageReport report = getReportFromDatabase(date, domain);

		return report;
	}

	private StorageReport getReportFromDatabase(long timestamp, String id) throws Exception {
		return m_reportService.queryReport(id, new Date(timestamp), new Date(timestamp + TimeHelper.ONE_HOUR));
	}

}
