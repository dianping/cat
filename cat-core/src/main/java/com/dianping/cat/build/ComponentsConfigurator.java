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
package com.dianping.cat.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.dianping.cat.config.app.*;
import com.dianping.cat.config.app.command.CommandFormatConfigManager;
import com.dianping.cat.config.app.command.DefaultCommandFormatlHandler;
import com.dianping.cat.config.web.AjaxDataTableProvider;
import com.dianping.cat.config.web.WebConfigManager;
import com.dianping.cat.config.web.WebSpeedConfigManager;
import com.dianping.cat.config.web.js.DefaultAggregationHandler;
import com.dianping.cat.config.web.url.DefaultUrlPatternHandler;
import com.dianping.cat.config.web.url.UrlPatternConfigManager;
import org.unidal.dal.jdbc.configuration.AbstractJdbcResourceConfigurator;
import org.unidal.lookup.configuration.Component;
import com.dianping.cat.CatConstants;
import com.dianping.cat.CatCoreModule;
import com.dianping.cat.analysis.DefaultMessageAnalyzerManager;
import com.dianping.cat.analysis.DefaultMessageHandler;
import com.dianping.cat.analysis.RealtimeConsumer;
import com.dianping.cat.analysis.TcpSocketReceiver;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.ReportReloadConfigManager;
import com.dianping.cat.config.business.BusinessConfigManager;
import com.dianping.cat.config.content.LocalResourceContentFetcher;
import com.dianping.cat.config.sample.SampleConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.server.ServerFilterConfigManager;
import com.dianping.cat.config.transaction.TpValueStatisticConfigManager;
import com.dianping.cat.message.DefaultPathBuilder;
import com.dianping.cat.message.storage.LocalMessageBucket;
import com.dianping.cat.report.DefaultReportBucketManager;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.LocalReportBucket;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.server.ServersUpdaterManager;
import com.dianping.cat.service.HostinfoService;
import com.dianping.cat.service.IpService;
import com.dianping.cat.service.IpService2;
import com.dianping.cat.statistic.ServerStatisticManager;
import com.dianping.cat.task.TaskManager;

public class ComponentsConfigurator extends AbstractJdbcResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(RealtimeConsumer.class));

		all.add(A(ServerConfigManager.class));
		all.add(A(HostinfoService.class));
		all.add(A(IpService.class));
		all.add(A(IpService2.class));
		all.add(A(TaskManager.class));
		all.add(A(ServerStatisticManager.class));
		all.add(A(DomainValidator.class));
		all.add(A(LocalResourceContentFetcher.class));
		all.add(A(ServerFilterConfigManager.class));

		all.add(A(DefaultPathBuilder.class));

		all.add(A(DefaultMessageAnalyzerManager.class));

		all.add(A(TcpSocketReceiver.class));

		all.add(A(DefaultMessageHandler.class));

		all.add(A(DefaultAggregationHandler.class));
		all.add(A(DefaultCommandFormatlHandler.class));
		all.add(A(CommandFormatConfigManager.class));
		all.add(A(SampleConfigManager.class));
		all.add(A(AppCommandConfigManager.class));
		all.add(A(AppCommandGroupConfigManager.class));
		all.add(A(WebConfigManager.class));
		all.add(A(WebSpeedConfigManager.class));
		all.add(A(AppSpeedConfigManager.class));
		all.add(A(BusinessConfigManager.class));
		all.add(A(MobileConfigManager.class));
		all.add(A(CrashLogConfigManager.class));
		all.add(A(SdkConfigManager.class));
		all.add(A(ReportReloadConfigManager.class));

		all.add(A(DefaultUrlPatternHandler.class));
		all.add(A(UrlPatternConfigManager.class));

		all.add(A(CatCoreModule.class));

		all.addAll(defineStorageComponents());

		all.add(A(RemoteServersManager.class));
		all.add(A(ServersUpdaterManager.class));

		all.add(A(AppCommandTableProvider.class));
		all.add(A(AppConnectionTableProvider.class));
		all.add(A(AppSpeedTableProvider.class));
		all.add(A(AjaxDataTableProvider.class));
		all.add(A(CrashLogTableProvider.class));
		all.add(A(CrashLogContentTableProvider.class));

		all.add(A(TpValueStatisticConfigManager.class));
		all.add(A(AtomicMessageConfigManager.class));

		all.add(defineJdbcDataSourceConfigurationManagerComponent("datasources.xml")
				.config(E("baseDirRef").value("CAT_HOME"))
				.config(E("defaultBaseDir").value(CatConstants.CAT_HOME_DEFAULT_DIR)));

		all.addAll(new CatCoreDatabaseConfigurator().defineComponents());
		all.addAll(new CatDatabaseConfigurator().defineComponents());
		all.addAll(new AppDatabaseConfigurator().defineComponents());
		all.addAll(new AppCrashDatabaseConfigurator().defineComponents());
		all.addAll(new WebDatabaseConfigurator().defineComponents());

		return all;
	}

	private Collection<Component> defineStorageComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(DefaultReportBucketManager.class));
		all.add(A(LocalReportBucket.class));
		all.add(A(LocalMessageBucket.class));

		return all;
	}

}
