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

import java.util.List;

import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageReportMerger;
import com.dianping.cat.consumer.storage.model.entity.StorageReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeStorageService extends BaseCompositeModelService<StorageReport> {
	public CompositeStorageService() {
		super(StorageAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<StorageReport> createRemoteService() {
		return new RemoteStorageService();
	}

	@Override
	protected StorageReport merge(ModelRequest request, List<ModelResponse<StorageReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		StorageReportMerger merger = new StorageReportMerger(new StorageReport(request.getDomain()));

		for (ModelResponse<StorageReport> response : responses) {
			if (response != null) {
				StorageReport model = response.getModel();
				if (model != null) {
					model.accept(merger);
				}
			}
		}
		return merger.getStorageReport();
	}
}
