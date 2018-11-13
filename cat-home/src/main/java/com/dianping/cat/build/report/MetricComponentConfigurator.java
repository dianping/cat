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

import com.dianping.cat.alarm.spi.config.AlertConfigManager;
import com.dianping.cat.alarm.spi.decorator.Decorator;
import com.dianping.cat.alarm.spi.receiver.Contactor;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.business.BusinessContactor;
import com.dianping.cat.report.alert.business.BusinessDecorator;
import com.dianping.cat.report.alert.business.BusinessReportGroupService;
import com.dianping.cat.report.alert.business.BusinessRuleConfigManager;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.graph.metric.impl.DataExtractorImpl;
import com.dianping.cat.report.page.business.service.BusinessReportService;
import com.dianping.cat.report.page.business.service.CompositeBusinessService;
import com.dianping.cat.report.page.business.service.HistoricalBusinessService;
import com.dianping.cat.report.page.business.service.LocalBusinessService;
import com.dianping.cat.report.page.business.task.BusinessBaselineReportBuilder;
import com.dianping.cat.report.page.business.task.BusinessKeyHelper;
import com.dianping.cat.report.page.business.task.BusinessPointParser;
import com.dianping.cat.report.page.metric.service.DefaultBaselineService;
import com.dianping.cat.report.page.metric.task.BaselineConfigManager;
import com.dianping.cat.report.page.metric.task.DefaultBaselineCreator;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

public class MetricComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(BusinessRuleConfigManager.class));

		all.add(A(BusinessReportService.class));

		all.add(A(DataExtractorImpl.class));

		all.add(A(BusinessReportGroupService.class));

		all.add(A(LocalBusinessService.class));
		all.add(C(ModelService.class, "business-historical", HistoricalBusinessService.class) //
								.req(BusinessReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, BusinessAnalyzer.ID, CompositeBusinessService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "business-historical" }, "m_services"));

		all.add(A(BaselineConfigManager.class));
		all.add(A(BusinessPointParser.class));
		all.add(A(BusinessKeyHelper.class));
		all.add(A(DefaultBaselineCreator.class));
		all.add(A(DefaultBaselineService.class));
		all.add(A(BusinessBaselineReportBuilder.class));

		all.add(C(Contactor.class, BusinessContactor.ID, BusinessContactor.class)
								.req(ProjectService.class, AlertConfigManager.class));

		all.add(C(Decorator.class, BusinessDecorator.ID, BusinessDecorator.class)
								.req(ProjectService.class, AlertSummaryExecutor.class));

		all.add(A(BusinessAlert.class));

		return all;
	}
}
