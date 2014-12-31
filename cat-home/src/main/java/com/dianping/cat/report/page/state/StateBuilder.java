package com.dianping.cat.report.page.state;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unidal.lookup.annotation.Inject;
import org.unidal.tuple.Pair;

import com.dianping.cat.Constants;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.consumer.state.StateAnalyzer;
import com.dianping.cat.consumer.state.model.entity.Machine;
import com.dianping.cat.consumer.state.model.entity.StateReport;
import com.dianping.cat.report.page.model.spi.ModelService;
import com.dianping.cat.service.ModelRequest;
import com.dianping.cat.service.ModelResponse;

public class StateBuilder {

	@Inject
	private ServerConfigManager m_configManager;

	@Inject(type = ModelService.class, value = StateAnalyzer.ID)
	private ModelService<StateReport> m_stateService;

	private String buildCatInfoMessage(StateReport report) {
		int realSize = report.getMachines().size();
		List<Pair<String, Integer>> servers = m_configManager.getConsoleEndpoints();
		int excepeted = servers.size();
		Set<String> errorServers = new HashSet<String>();

		if (realSize != excepeted) {
			for (Pair<String, Integer> server : servers) {
				String serverIp = server.getKey();

				if (report.getMachines().get(serverIp) == null) {
					errorServers.add(serverIp);
				}
			}
		}
		for (Machine machine : report.getMachines().values()) {
			if (machine.getTotalLoss() > 300 * 10000) {
				errorServers.add(machine.getIp());
			}
		}

		if (errorServers.size() > 0) {
			return errorServers.toString();
		} else {
			return null;
		}
	}

	public String buildStateMessage(long date, String ip) {
		return buildCatInfoMessage(queryHourlyReport(date, ip));
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
