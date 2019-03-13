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

import org.unidal.cat.message.storage.hdfs.HdfsBucketManager;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.hadoop.hdfs.HdfsMessageBucketManager;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.page.cross.service.CompositeCrossService;
import com.dianping.cat.report.page.cross.service.CrossReportService;
import com.dianping.cat.report.page.cross.service.HistoricalCrossService;
import com.dianping.cat.report.page.cross.service.LocalCrossService;
import com.dianping.cat.report.page.cross.task.CrossReportBuilder;
import com.dianping.cat.report.page.logview.service.CompositeLogViewService;
import com.dianping.cat.report.page.logview.service.HistoricalMessageService;
import com.dianping.cat.report.page.logview.service.LocalMessageService;
import com.dianping.cat.report.page.matrix.service.CompositeMatrixService;
import com.dianping.cat.report.page.matrix.service.HistoricalMatrixService;
import com.dianping.cat.report.page.matrix.service.LocalMatrixService;
import com.dianping.cat.report.page.matrix.service.MatrixReportService;
import com.dianping.cat.report.page.matrix.task.MatrixReportBuilder;
import com.dianping.cat.report.page.state.StateGraphBuilder;
import com.dianping.cat.report.page.state.service.CompositeStateService;
import com.dianping.cat.report.page.state.service.HistoricalStateService;
import com.dianping.cat.report.page.state.service.LocalStateService;
import com.dianping.cat.report.page.state.service.StateReportService;
import com.dianping.cat.report.page.state.task.StateReportBuilder;
import com.dianping.cat.report.page.statistics.service.ClientReportService;
import com.dianping.cat.report.page.statistics.service.HeavyReportService;
import com.dianping.cat.report.page.statistics.service.JarReportService;
import com.dianping.cat.report.page.statistics.service.ServiceReportService;
import com.dianping.cat.report.page.statistics.service.UtilizationReportService;
import com.dianping.cat.report.page.statistics.task.heavy.HeavyReportBuilder;
import com.dianping.cat.report.page.statistics.task.jar.JarReportBuilder;
import com.dianping.cat.report.page.statistics.task.service.ClientReportBuilder;
import com.dianping.cat.report.page.statistics.task.service.ServiceReportBuilder;
import com.dianping.cat.report.page.statistics.task.utilization.UtilizationReportBuilder;
import com.dianping.cat.report.page.top.service.CompositeTopService;
import com.dianping.cat.report.page.top.service.HistoricalTopService;
import com.dianping.cat.report.page.top.service.LocalTopService;
import com.dianping.cat.report.page.top.service.TopReportService;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.system.page.router.config.RouterConfigAdjustor;
import com.dianping.cat.system.page.router.config.RouterConfigHandler;
import com.dianping.cat.system.page.router.config.RouterConfigManager;
import com.dianping.cat.system.page.router.service.CachedRouterConfigService;
import com.dianping.cat.system.page.router.service.RouterConfigService;
import com.dianping.cat.system.page.router.task.RouterConfigBuilder;

public class ReportComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(UtilizationReportService.class));
		all.add(A(UtilizationReportBuilder.class));

		all.add(A(ServiceReportService.class));
		all.add(A(ServiceReportBuilder.class));

		all.add(A(HeavyReportService.class));
		all.add(A(HeavyReportBuilder.class));

		all.add(A(RouterConfigManager.class));
		all.add(A(RouterConfigHandler.class));
		all.add(A(RouterConfigService.class));
		all.add(A(CachedRouterConfigService.class));
		all.add(A(RouterConfigAdjustor.class));
		all.add(A(RouterConfigBuilder.class));

		all.add(A(JarReportService.class));
		all.add(A(JarReportBuilder.class));

		all.add(A(ClientReportService.class));
		all.add(A(ClientReportBuilder.class));

		// cross report
		all.add(A(CrossReportService.class));
		all.add(A(CrossReportBuilder.class));

		all.add(A(LocalCrossService.class));
		all.add(C(ModelService.class, "cross-historical", HistoricalCrossService.class) //
								.req(CrossReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, CrossAnalyzer.ID, CompositeCrossService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "cross-historical" }, "m_services"));

		// matrix report
		all.add(A(MatrixReportService.class));
		all.add(A(MatrixReportBuilder.class));

		all.add(A(LocalMatrixService.class));
		all.add(C(ModelService.class, "matrix-historical", HistoricalMatrixService.class) //
								.req(MatrixReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, MatrixAnalyzer.ID, CompositeMatrixService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "matrix-historical" }, "m_services"));

		// state report
		all.add(A(StateReportService.class));
		all.add(A(StateReportBuilder.class));
		all.add(A(StateGraphBuilder.class));

		all.add(A(LocalStateService.class));
		all.add(C(ModelService.class, "state-historical", HistoricalStateService.class) //
								.req(StateReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, StateAnalyzer.ID, CompositeStateService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "state-historical" }, "m_services"));

		// top report
		all.add(A(TopReportService.class));

		all.add(A(LocalTopService.class));
		all.add(C(ModelService.class, "top-historical", HistoricalTopService.class) //
								.req(TopReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, TopAnalyzer.ID, CompositeTopService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "top-historical" }, "m_services"));

		// message service
		all.add(A(LocalMessageService.class));
		all.add(C(ModelService.class, "logview-historical", HistoricalMessageService.class) //
								.req(MessageBucketManager.class, HdfsMessageBucketManager.ID) //
								.req(HdfsBucketManager.class).req(ServerConfigManager.class));
		all.add(C(ModelService.class, "logview", CompositeLogViewService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "logview-historical" }, "m_services"));

		return all;
	}
}
