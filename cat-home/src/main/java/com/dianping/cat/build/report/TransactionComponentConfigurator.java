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
import com.dianping.cat.consumer.transaction.TransactionAnalyzer;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import com.dianping.cat.report.alert.transaction.TransactionContactor;
import com.dianping.cat.report.alert.transaction.TransactionDecorator;
import com.dianping.cat.report.alert.transaction.TransactionRuleConfigManager;
import com.dianping.cat.report.page.transaction.service.CompositeTransactionService;
import com.dianping.cat.report.page.transaction.service.HistoricalTransactionService;
import com.dianping.cat.report.page.transaction.service.LocalTransactionService;
import com.dianping.cat.report.page.transaction.service.TransactionReportService;
import com.dianping.cat.report.page.transaction.task.TransactionReportBuilder;
import com.dianping.cat.report.page.transaction.transform.TransactionMergeHelper;
import com.dianping.cat.report.server.RemoteServersManager;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.service.ProjectService;

public class TransactionComponentConfigurator extends AbstractResourceConfigurator {
	@Override
	public List<Component> defineComponents() {
		List<Component> all = new ArrayList<Component>();

		all.add(A(TransactionMergeHelper.class));
		all.add(A(TransactionReportService.class));
		all.add(A(TransactionRuleConfigManager.class));

		all.add(C(Contactor.class, TransactionContactor.ID, TransactionContactor.class)
								.req(ProjectService.class,	AlertConfigManager.class));
		all.add(C(Decorator.class, TransactionDecorator.ID, TransactionDecorator.class));
		all.add(A(TransactionAlert.class));

		all.add(A(LocalTransactionService.class));
		all.add(C(ModelService.class, "transaction-historical", HistoricalTransactionService.class) //
								.req(TransactionReportService.class, ServerConfigManager.class));
		all.add(C(ModelService.class, TransactionAnalyzer.ID, CompositeTransactionService.class) //
								.req(ServerConfigManager.class, RemoteServersManager.class) //
								.req(ModelService.class, new String[] { "transaction-historical" }, "m_services"));

		all.add(A(TransactionReportBuilder.class));

		return all;
	}
}
