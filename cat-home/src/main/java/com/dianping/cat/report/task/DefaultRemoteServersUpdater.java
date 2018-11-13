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
package com.dianping.cat.report.task;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Constants;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.ProcessDomain;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;
import com.dianping.cat.report.server.ServersUpdater;
import com.dianping.cat.report.service.ModelPeriod;
import com.dianping.cat.report.service.ModelRequest;
import com.dianping.cat.report.service.ModelResponse;
import com.dianping.cat.report.service.ModelService;

@Named(type = ServersUpdater.class)
public class DefaultRemoteServersUpdater implements ServersUpdater {

	@Inject(type = ModelService.class, value = StateAnalyzer.ID)
	private ModelService<StateReport> m_service;

	@Override
	public Map<String, Set<String>> buildServers(Date hour) {
		StateReport currentReport = queryStateReport(Constants.CAT, hour.getTime());
		StateReportVisitor visitor = new StateReportVisitor();

		visitor.visitStateReport(currentReport);
		return visitor.getServers();
	}

	public StateReport queryStateReport(String domain, long time) {
		ModelPeriod period = ModelPeriod.getByTime(time);

		if (period == ModelPeriod.CURRENT || period == ModelPeriod.LAST) {
			ModelRequest request = new ModelRequest(domain, time);

			if (m_service.isEligable(request)) {
				ModelResponse<StateReport> response = m_service.invoke(request);
				StateReport report = response.getModel();

				return report;
			} else {
				throw new RuntimeException("Internal error: no eligable state report service registered for " + request	+ "!");
			}
		} else {
			throw new RuntimeException("Domain server update period is not right: " + period + ", time is: "	+ new Date(time));
		}
	}

	public static class StateReportVisitor extends BaseVisitor {

		private Map<String, Set<String>> m_servers = new ConcurrentHashMap<String, Set<String>>();

		private String m_ip;

		public Map<String, Set<String>> getServers() {
			return m_servers;
		}

		@Override
		public void visitMachine(Machine machine) {
			m_ip = machine.getIp();
			super.visitMachine(machine);
		}

		@Override
		public void visitProcessDomain(ProcessDomain processDomain) {
			if (processDomain.getTotal() > 0) {
				String domain = processDomain.getName();
				Set<String> servers = m_servers.get(domain);

				if (servers == null) {
					servers = new HashSet<String>();

					m_servers.put(domain, servers);
				}
				servers.add(m_ip);
			}
		}
	}

}
