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
package com.dianping.cat.report.server;

import java.util.Date;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeHelper;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class ServersUpdaterManager implements Initializable {

	@Inject
	private ServersUpdater m_remoteServerUpdater;

	@Inject
	private RemoteServersManager m_remoteServersManager;

	@Override
	public void initialize() throws InitializationException {
		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return "remote-server-updater";
			}

			@Override
			public void handle() throws Exception {
				try {
					long currentHour = TimeHelper.getCurrentHour().getTime();
					Map<String, Set<String>> currentServers = m_remoteServerUpdater.buildServers(new Date(currentHour));

					m_remoteServersManager.setCurrentServers(currentServers);

					long lastHour = currentHour - TimeHelper.ONE_HOUR;
					Map<String, Set<String>> lastServers = m_remoteServerUpdater.buildServers(new Date(lastHour));

					m_remoteServersManager.setLastServers(lastServers);
				} catch (Exception e) {
					Cat.logError(e);
				}
			}
		});
	}
}
