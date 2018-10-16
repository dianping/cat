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
package com.dianping.cat.report.page.state;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.home.router.entity.DefaultServer;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;
import com.dianping.cat.system.page.router.config.RouterConfigManager;

public class StateBuilder {

	public static final int COUNT = 500 * 10000;

	@Inject
	private RouterConfigManager m_routerManager;

	@Inject(type = ModelService.class, value = StateAnalyzer.ID)
	private ModelService<StateReport> m_stateService;

	public static boolean checkTooMuchLoss(Machine machine) {
		return machine.getTotalLoss() > COUNT;
	}

	public String buildStateMessage(long date, String ip) {
		StateReport report = queryHourlyReport(date, ip);

		if (report != null) {
			int realSize = report.getMachines().size();
			List<String> servers = queryAllServers();
			int excepeted = servers.size();
			Set<String> errorServers = new HashSet<String>();

			if (realSize != excepeted) {
				for (String serverIp : servers) {
					if (report.getMachines().get(serverIp) == null) {
						errorServers.add(serverIp);
					}
				}
			}
			for (Machine machine : report.getMachines().values()) {
				if (checkTooMuchLoss(machine)) {
					errorServers.add(machine.getIp());
				}
			}

			if (errorServers.size() > 0) {
				return errorServers.toString();
			}
		}
		return null;
	}

	private List<String> queryAllServers() {
		List<String> strs = new ArrayList<String>();
		String backUpServer = m_routerManager.getRouterConfig().getBackupServer();
		Map<String, DefaultServer> servers = m_routerManager.getRouterConfig().getDefaultServers();

		for (Entry<String, DefaultServer> server : servers.entrySet()) {
			strs.add(server.getValue().getId());
		}
		strs.add(backUpServer);
		return strs;
	}

	private StateReport queryHourlyReport(long date, String ip) {
		String domain = Constants.CAT;
		ModelRequest request = new ModelRequest(domain, date) //
								.setProperty("ip", ip);

		if (m_stateService.isEligable(request)) {
			ModelResponse<StateReport> response = m_stateService.invoke(request);

			return response.getModel();
		} else {
			throw new RuntimeException("Internal error: no eligable sql service registered for " + request + "!");
		}
	}
}
