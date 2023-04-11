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
package com.dianping.cat.report.alert;

import com.dianping.cat.report.alert.business.BusinessAlert;
import com.dianping.cat.report.alert.event.EventAlert;
import com.dianping.cat.report.alert.exception.ExceptionAlert;
import com.dianping.cat.report.alert.heartbeat.HeartbeatAlert;
import com.dianping.cat.report.alert.storage.cache.StorageCacheAlert;
import com.dianping.cat.report.alert.storage.rpc.StorageRPCAlert;
import com.dianping.cat.report.alert.storage.sql.StorageSQLAlert;
import com.dianping.cat.report.alert.transaction.TransactionAlert;
import org.unidal.helper.Threads;
import org.unidal.lookup.ContainerHolder;
import org.unidal.lookup.annotation.Named;

@Named
public class AlarmManager extends ContainerHolder {

	public void startAlarm() {
//		Map<String, ServerAlarm> serverAlarms = lookupMap(ServerAlarm.class);
//		for (ServerAlarm serverAlarm : serverAlarms.values()) {
//			Threads.forGroup("cat").start(serverAlarm);
//		}

		BusinessAlert businessAlert = lookup(BusinessAlert.class);
		ExceptionAlert exceptionAlert = lookup(ExceptionAlert.class);
		HeartbeatAlert heartbeatAlert = lookup(HeartbeatAlert.class);
		TransactionAlert transactionAlert = lookup(TransactionAlert.class);
		EventAlert eventAlert = lookup(EventAlert.class);
		Threads.forGroup("cat").start(businessAlert);
		Threads.forGroup("cat").start(exceptionAlert);
		Threads.forGroup("cat").start(heartbeatAlert);
		Threads.forGroup("cat").start(transactionAlert);
		Threads.forGroup("cat").start(eventAlert);

		StorageSQLAlert storageDatabaseAlert = lookup(StorageSQLAlert.class);
		StorageCacheAlert storageCacheAlert = lookup(StorageCacheAlert.class);
		StorageRPCAlert storageRpcAlert = lookup(StorageRPCAlert.class);
		Threads.forGroup("cat").start(storageDatabaseAlert);
		Threads.forGroup("cat").start(storageCacheAlert);
		Threads.forGroup("cat").start(storageRpcAlert);

//		ThirdPartyAlert thirdPartyAlert = lookup(ThirdPartyAlert.class);
//		ThirdPartyAlertBuilder alertBuildingTask = lookup(ThirdPartyAlertBuilder.class);
//		AppAlert appAlert = lookup(AppAlert.class);
//		JsAlert jsAlert = lookup(JsAlert.class);
//		AjaxAlert ajaxAlert = lookup(AjaxAlert.class);
//		CrashAlert crashAlert = lookup(CrashAlert.class);
//		Threads.forGroup("cat").start(thirdPartyAlert);
//		Threads.forGroup("cat").start(alertBuildingTask);
//		Threads.forGroup("cat").start(appAlert);
//		Threads.forGroup("cat").start(jsAlert);
//		Threads.forGroup("cat").start(ajaxAlert);
//		Threads.forGroup("cat").start(crashAlert);
	}
}
