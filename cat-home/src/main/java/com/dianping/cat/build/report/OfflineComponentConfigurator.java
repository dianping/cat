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
package com.dianping.cat.build.report;

import java.util.ArrayList;
import java.util.List;

import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.report.page.overload.task.CapacityUpdateStatusManager;
import com.dianping.cat.report.page.overload.task.CapacityUpdateTask;
import com.dianping.cat.report.page.overload.task.DailyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.HourlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.MonthlyCapacityUpdater;
import com.dianping.cat.report.page.overload.task.TableCapacityService;
import com.dianping.cat.report.page.overload.task.WeeklyCapacityUpdater;
import com.dianping.cat.report.task.cmdb.CmdbInfoReloadBuilder;
import com.dianping.cat.report.task.current.CurrentReportBuilder;

public class OfflineComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(CapacityUpdateStatusManager.class));
		all.add(A(HourlyCapacityUpdater.class));
		all.add(A(DailyCapacityUpdater.class));
		all.add(A(WeeklyCapacityUpdater.class));
		all.add(A(MonthlyCapacityUpdater.class));
		all.add(A(TableCapacityService.class));
		all.add(A(CapacityUpdateTask.class));

		all.add(A(CurrentReportBuilder.class));
		all.add(A(CmdbInfoReloadBuilder.class));

		return all;
	}
}
