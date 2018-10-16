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
package com.dianping.cat.consumer.build;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.unidal.initialization.Module;
import org.unidal.lookup.configuration.AbstractResourceConfigurator;
import org.unidal.lookup.configuration.Component;

import com.dianping.cat.analysis.MessageAnalyzer;
import com.dianping.cat.config.AtomicMessageConfigManager;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.config.transaction.TpValueStatisticConfigManager;
import com.dianping.cat.consumer.CatConsumerModule;
import com.dianping.cat.consumer.DatabaseParser;
import com.dianping.cat.consumer.business.BusinessAnalyzer;
import com.dianping.cat.consumer.business.BusinessDelegate;
import com.dianping.cat.consumer.config.AllReportConfigManager;
import com.dianping.cat.consumer.cross.CrossAnalyzer;
import com.dianping.cat.consumer.cross.CrossDelegate;
import com.dianping.cat.consumer.cross.IpConvertManager;
import com.dianping.cat.consumer.dependency.DependencyAnalyzer;
import com.dianping.cat.consumer.dependency.DependencyDelegate;
import com.dianping.cat.consumer.dump.DumpAnalyzer;
import com.dianping.cat.consumer.dump.LocalMessageBucketManager;
import com.dianping.cat.consumer.event.EventAnalyzer;
import com.dianping.cat.consumer.event.EventDelegate;
import com.dianping.cat.consumer.heartbeat.HeartbeatAnalyzer;
import com.dianping.cat.consumer.heartbeat.HeartbeatDelegate;
import com.dianping.cat.consumer.matrix.MatrixAnalyzer;
import com.dianping.cat.consumer.matrix.MatrixDelegate;
import com.dianping.cat.consumer.problem.DefaultProblemHandler;
import com.dianping.cat.consumer.problem.LongExecutionProblemHandler;
import com.dianping.cat.consumer.problem.ProblemAnalyzer;
import com.dianping.cat.consumer.problem.ProblemDelegate;
import com.dianping.cat.consumer.problem.ProblemHandler;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.StateDelegate;
import com.dianping.cat.consumer.storage.StorageAnalyzer;
import com.dianping.cat.consumer.storage.StorageDelegate;
import com.dianping.cat.consumer.storage.StorageReportUpdater;
import com.dianping.cat.consumer.storage.builder.StorageBuilderManager;
import com.dianping.cat.consumer.storage.builder.StorageCacheBuilder;
import com.dianping.cat.consumer.storage.builder.StorageRPCBuilder;
import com.dianping.cat.consumer.storage.builder.StorageSQLBuilder;
import com.dianping.cat.consumer.top.TopAnalyzer;
import com.dianping.cat.consumer.top.TopDelegate;
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.consumer.transaction.TransactionDelegate;
import com.dianping.cat.core.dal.HourlyReportContentDao;
import com.dianping.cat.core.dal.HourlyReportDao;
import com.dianping.cat.message.PathBuilder;
import com.dianping.cat.message.storage.MessageBucketManager;
import com.dianping.cat.report.DefaultReportManager;
import com.dianping.cat.report.DomainValidator;
import com.dianping.cat.report.ReportBucketManager;
import com.dianping.cat.report.ReportDelegate;
import com.dianping.cat.report.ReportManager;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.statistic.ServerStatisticManager;

public class ComponentsConfigurator extends AbstractResourceConfigurator {
	public static void main(String[] args) {
		generatePlexusComponentsXmlFile(new ComponentsConfigurator());
	}

	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.addAll(defineTransactionComponents());
		all.addAll(defineEventComponents());
		all.addAll(defineProblemComponents());
		all.addAll(defineHeartbeatComponents());
		all.addAll(defineTopComponents());
		all.addAll(defineDumpComponents());
		all.addAll(defineStateComponents());
		all.addAll(defineCrossComponents());
		all.addAll(defineMatrixComponents());
		all.addAll(defineDependencyComponents());
		all.addAll(defineStorageComponents());
		all.addAll(defineBusinessComponents());

		all.add(A(AtomicMessageConfigManager.class));
		all.add(A(ServerConfigManager.class));
		all.add(A(TpValueStatisticConfigManager.class));
		all.add(A(AllReportConfigManager.class));
		all.add(C(Module.class, CatConsumerModule.ID, CatConsumerModule.class));

		return all;
	}

	private Collection<Component> defineCrossComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = CrossAnalyzer.ID;

		all.add(A(CrossAnalyzer.class));
		all.add(A(CrossDelegate.class));

		all.add(C(IpConvertManager.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineDependencyComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = DependencyAnalyzer.ID;

		all.add(A(DependencyAnalyzer.class));
		all.add(A(DependencyDelegate.class));

		all.add(C(DatabaseParser.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineDumpComponents() {
		final List<Component> all = new ArrayList<Component>();
		all.add(A(DumpAnalyzer.class));

		all.add(C(MessageBucketManager.class, LocalMessageBucketManager.ID, LocalMessageBucketManager.class) //
								.req(ServerConfigManager.class, PathBuilder.class, ServerStatisticManager.class));

		return all;
	}

	private Collection<Component> defineEventComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = EventAnalyzer.ID;

		all.add(A(EventAnalyzer.class));
		all.add(A(EventDelegate.class));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineHeartbeatComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = HeartbeatAnalyzer.ID;

		all.add(A(HeartbeatAnalyzer.class));
		all.add(A(HeartbeatDelegate.class));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineMatrixComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = MatrixAnalyzer.ID;

		all.add(A(MatrixAnalyzer.class));
		all.add(A(MatrixDelegate.class));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineBusinessComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = BusinessAnalyzer.ID;

		all.add(A(BusinessAnalyzer.class));
		all.add(A(BusinessDelegate.class));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));
		return all;
	}

	private Collection<Component> defineProblemComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = ProblemAnalyzer.ID;

		all.add(C(ProblemHandler.class, DefaultProblemHandler.ID, DefaultProblemHandler.class)//
								.config(E("errorType").value("Error,RuntimeException,Exception"))//
								.req(ServerConfigManager.class));

		all.add(C(ProblemHandler.class, LongExecutionProblemHandler.ID, LongExecutionProblemHandler.class) //
								.req(ServerConfigManager.class));

		all.add(C(MessageAnalyzer.class, ID, ProblemAnalyzer.class).is(PER_LOOKUP) //
								.req(ReportManager.class, ID).req(ServerConfigManager.class).req(ProblemHandler.class, //
														new String[] { DefaultProblemHandler.ID, LongExecutionProblemHandler.ID }, "m_handlers"));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		all.add(A(ProblemDelegate.class));

		return all;
	}

	private Collection<Component> defineStateComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = StateAnalyzer.ID;

		all.add(A(StateAnalyzer.class));
		all.add(A(StateDelegate.class));

		all.add(A(ProjectService.class));
		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineTopComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = TopAnalyzer.ID;

		all.add(A(TopAnalyzer.class).config(E("errorType").value("Error,RuntimeException,Exception")));
		all.add(A(TopDelegate.class));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineTransactionComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = TransactionAnalyzer.ID;

		all.add(A(TransactionAnalyzer.class));
		all.add(A(TransactionDelegate.class));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}

	private Collection<Component> defineStorageComponents() {
		final List<Component> all = new ArrayList<Component>();
		final String ID = StorageAnalyzer.ID;

		all.add(A(StorageReportUpdater.class));
		all.add(A(StorageBuilderManager.class));
		all.add(A(StorageSQLBuilder.class));
		all.add(A(StorageCacheBuilder.class));
		all.add(A(StorageRPCBuilder.class));

		all.add(A(StorageAnalyzer.class));
		all.add(A(StorageDelegate.class));

		all.add(C(ReportManager.class, ID, DefaultReportManager.class).is(PER_LOOKUP) //
								.req(ReportDelegate.class, ID) //
								.req(ReportBucketManager.class, HourlyReportDao.class, HourlyReportContentDao.class, DomainValidator.class) //
								.config(E("name").value(ID)));

		return all;
	}
}
