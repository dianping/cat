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
package com.dianping.cat.mvc;

import java.util.Date;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.helper.TimeHelper;

@Named
public class PayloadNormalizer {

	@Inject
	protected ServerConfigManager m_manager;

	@SuppressWarnings("rawtypes")
	public void normalize(AbstractReportModel model, AbstractReportPayload payload) {
		long date = payload.getDate();
		long current = System.currentTimeMillis();

		if (date > current) {
			date = current - current % TimeHelper.ONE_HOUR;
			model.setDate(date);
		} else {
			model.setDate(date);
		}

		model.setIpAddress(payload.getIpAddress());
		model.setDisplayDomain(payload.getDomain());

		if (payload.getAction().getName().startsWith("history")) {
			payload.computeHistoryDate();

			Date start = payload.getHistoryStartDate();
			Date end = payload.getHistoryEndDate();

			model.setReportType(payload.getReportType());
			model.setDate(start.getTime());
			model.setCustomDate(start, end);
		}
	}

}
