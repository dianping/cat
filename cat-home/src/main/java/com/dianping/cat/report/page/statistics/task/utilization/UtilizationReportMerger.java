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
package com.dianping.cat.report.page.statistics.task.utilization;

import com.dianping.cat.home.utilization.entity.ApplicationState;
import com.dianping.cat.home.utilization.entity.Domain;
import com.dianping.cat.home.utilization.entity.MachineState;
import com.dianping.cat.home.utilization.entity.UtilizationReport;
import com.dianping.cat.home.utilization.transform.DefaultMerger;

public class UtilizationReportMerger extends DefaultMerger {

	public UtilizationReportMerger(UtilizationReport utilizationReport) {
		super(utilizationReport);
	}

	@Override
	protected void mergeApplicationState(ApplicationState to, ApplicationState from) {
		to.setAvg95((to.getAvg95() * to.getCount() + from.getAvg95() * from.getCount())	/ (to.getCount() + from.getCount()));
		if (from.getMaxQps() > to.getMaxQps()) {
			to.setMaxQps(from.getMaxQps());
		}
		to.setSum(to.getSum() + from.getSum());
		to.setCount(to.getCount() + from.getCount());
		to.setFailureCount(to.getFailureCount() + from.getFailureCount());
		to.setAvg(to.getSum() / to.getCount());
		to.setFailurePercent(to.getFailureCount() * 1.0 / to.getCount());
	}

	@Override
	protected void mergeDomain(Domain old, Domain domain) {
		if (domain.getMachineNumber() > old.getMachineNumber()) {
			old.setMachineNumber(domain.getMachineNumber());
		}
	}

	@Override
	protected void mergeMachineState(MachineState to, MachineState from) {
		if (from.getAvgMax() > to.getAvgMax()) {
			to.setAvgMax(from.getAvgMax());
		}
		to.setSum(to.getSum() + from.getSum());
		to.setCount(to.getCount() + from.getCount());
		to.setAvg(to.getSum() * 1.0 / to.getCount());
	}

	@Override
	protected void mergeUtilizationReport(UtilizationReport old, UtilizationReport bugReport) {
		super.mergeUtilizationReport(old, bugReport);
	}

	@Override
	public void visitUtilizationReport(UtilizationReport utilizationReport) {
		UtilizationReport oldReport = getUtilizationReport();

		oldReport.setDomain(utilizationReport.getDomain());
		oldReport.setStartTime(utilizationReport.getStartTime());
		oldReport.setEndTime(utilizationReport.getEndTime());
		super.visitUtilizationReport(utilizationReport);
	}

}
