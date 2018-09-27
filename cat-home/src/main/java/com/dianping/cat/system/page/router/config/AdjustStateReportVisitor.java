package com.dianping.cat.system.page.router.config;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.transform.BaseVisitor;

public class AdjustStateReportVisitor extends BaseVisitor {

	private RouterConfigManager m_routerConfigManager;

	private List<String> m_servers;

	Map<String, Map<String, Machine>> m_datas = new HashMap<String, Map<String, Machine>>();

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
