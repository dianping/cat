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
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.report.alert.exception.AlertExceptionBuilder;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.exception.ExceptionContactor;
import com.dianping.cat.report.alert.exception.ExceptionDecorator;
import com.dianping.cat.report.alert.exception.ExceptionRuleConfigManager;
import com.dianping.cat.report.alert.summary.AlertSummaryExecutor;
import com.dianping.cat.report.alert.summary.AlertSummaryService;
import com.dianping.cat.report.alert.summary.build.AlertInfoBuilder;
import com.dianping.cat.report.alert.summary.build.AlterationSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.FailureSummaryBuilder;
import com.dianping.cat.report.alert.summary.build.RelatedSummaryBuilder;
import com.dianping.cat.report.page.problem.service.CompositeProblemService;
import com.dianping.cat.report.page.problem.service.HistoricalProblemService;
import com.dianping.cat.report.page.problem.service.LocalProblemService;
import com.dianping.cat.report.page.problem.service.ProblemReportService;
import com.dianping.cat.report.page.problem.task.ProblemReportBuilder;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

public class ProblemComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(ExceptionRuleConfigManager.class));

		all.add(A(ProblemReportService.class));
		all.add(A(ProblemReportBuilder.class));

		all.add(A(LocalProblemService.class));
		all.add(C(ModelService.class, "problem-historical", HistoricalProblemService.class) //
								.req(ProblemReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, ProblemAnalyzer.ID, CompositeProblemService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "problem-historical" }, "m_services"));

		all.add(C(Contactor.class, ExceptionContactor.ID, ExceptionContactor.class)
								.req(ProjectService.class,	AlertConfigManager.class));
		all.add(C(Decorator.class, ExceptionDecorator.ID, ExceptionDecorator.class)
								.req(ProjectService.class,	AlertSummaryExecutor.class));

		all.add(A(AlertExceptionBuilder.class));

		all.add(A(ExceptionAlert.class));
		all.add(A(AlertSummaryService.class));
		all.add(A(RelatedSummaryBuilder.class));
		all.add(A(FailureSummaryBuilder.class));
		all.add(A(AlterationSummaryBuilder.class));
		all.add(A(AlertSummaryExecutor.class));
		all.add(A(AlertInfoBuilder.class));

		return all;
	}
}
