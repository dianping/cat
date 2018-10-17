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
package com.dianping.cat.system.page.router.service;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.home.router.entity.RouterConfig;
import com.dianping.cat.task.TimerSyncTask;
import com.dianping.cat.task.TimerSyncTask.SyncHandler;

@Named
public class CachedRouterConfigService implements Initializable {

	@Inject
	private RouterConfigService m_routerConfigService;

	private volatile RouterConfig m_routerConfig;

	@Override
	public void initialize() throws InitializationException {
		refresh();

		TimerSyncTask.getInstance().register(new SyncHandler() {

			@Override
			public String getName() {
				return "router-refresh-task";
			}

			@Override
			public void handle() throws Exception {
				refresh();
			}
		});
	}

	public RouterConfig queryLastRouterConfig() {
		return m_routerConfig;
	}

	public void refresh() {
		m_routerConfig = m_routerConfigService.queryLastReport(Constants.CAT);
	}

}
