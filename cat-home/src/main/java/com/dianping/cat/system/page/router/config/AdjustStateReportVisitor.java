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
package com.dianping.cat.system.page.router.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;

public class AdjustStateReportVisitor extends BaseVisitor {

	Map<String, Map<String, Machine>> m_datas = new HashMap<String, Map<String, Machine>>();

	private RouterConfigManager m_routerConfigManager;

	private List<String> m_servers;

	public AdjustStateReportVisitor(RouterConfigManager routerConfigManager, List<String> servers) {
		m_routerConfigManager = routerConfigManager;
		m_servers = servers;
	}

	public Map<String, Map<String, Machine>> getStatistics() {
		return m_datas;
	}

	@Override
	public void visitMachine(Machine machine) {
		String ip = machine.getIp();

		if (isConsumerMachine(ip)) {
			String group = m_routerConfigManager.queryServerGroupByIp(ip);
			Map<String, Machine> ms = m_datas.get(group);

			if (ms == null) {
				ms = new HashMap<String, Machine>();

				m_datas.put(group, ms);
			}
			ms.put(ip, machine);
		}
	}

	private boolean isConsumerMachine(String ip) {
		for (String server : m_servers) {
			if (server.startsWith(ip)) {
				return true;
			}
		}
		return false;
	}

}
