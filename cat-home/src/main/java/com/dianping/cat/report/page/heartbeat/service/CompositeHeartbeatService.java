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
package com.dianping.cat.report.page.heartbeat.service;

import java.util.List;

import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatReportMerger;
import com.dianping.cat.consumer.heartbeat.model.entity.HeartbeatReport;
import com.dianping.cat.report.service.BaseCompositeModelService;
import com.dianping.cat.report.service.BaseRemoteModelService;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;

public class CompositeHeartbeatService extends BaseCompositeModelService<HeartbeatReport> {
	public CompositeHeartbeatService() {
		super(HeartbeatAnalyzer.ID);
	}

	@Override
	protected BaseRemoteModelService<HeartbeatReport> createRemoteService() {
		return new RemoteHeartbeatService();
	}

	@Override
	protected HeartbeatReport merge(ModelRequest request, List<ModelResponse<HeartbeatReport>> responses) {
		if (responses.size() == 0) {
			return null;
		}
		HeartbeatReportMerger merger = new HeartbeatReportMerger(new HeartbeatReport(request.getDomain()));

		for (ModelResponse<HeartbeatReport> response : responses) {
			if (response != null) {
				HeartbeatReport model = response.getModel();

				if (model != null) {
					model.accept(merger);
				}
			}
		}
		return merger.getHeartbeatReport();
	}
}
