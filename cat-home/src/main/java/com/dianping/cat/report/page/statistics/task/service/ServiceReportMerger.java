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
package com.dianping.cat.report.page.statistics.task.service;

import com.dianping.cat.home.service.entity.Domain;
import com.dianping.cat.home.service.entity.ServiceReport;
import com.dianping.cat.home.service.transform.DefaultMerger;

public class ServiceReportMerger extends DefaultMerger {

	public ServiceReportMerger(ServiceReport serviceReport) {
		super(serviceReport);
	}

	@Override
	protected void mergeDomain(Domain to, Domain from) {
		to.setTotalCount(to.getTotalCount() + from.getTotalCount());
		to.setFailureCount(to.getFailureCount() + from.getFailureCount());
		to.setFailurePercent(to.getFailureCount() * 1.0 / to.getTotalCount());
		to.setSum(to.getSum() + from.getSum());
		to.setAvg(to.getSum() / to.getTotalCount());
	}

	@Override
	protected void mergeServiceReport(ServiceReport to, ServiceReport from) {
		to.setStartTime(from.getStartTime());
		to.setEndTime(from.getEndTime());
		to.setDomain(from.getDomain());
		super.mergeServiceReport(to, from);
	}

}
