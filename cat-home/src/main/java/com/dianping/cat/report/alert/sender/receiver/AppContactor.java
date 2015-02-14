package com.dianping.cat.report.alert.sender.receiver;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.report.alert.AlertType;

public class AppContactor extends ProjectContactor {

	@Inject
	protected AppConfigManager m_appConfigManager;

	public static final String ID = AlertType.App.getName();

	@Override
	public String getId() {
		return ID;
	}

	private String queryDomainByCommand(String command) {
		Map<Integer, Command> commands = m_appConfigManager.getRawCommands();
		String domain = "";

		for (Entry<Integer, Command> entry : commands.entrySet()) {
			Command commandObj = entry.getValue();

			if (commandObj.getName().equals(command)) {
				domain = commandObj.getDomain();
			}
		}
		return domain;
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		return super.queryEmailContactors(queryDomainByCommand(id));
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		return super.queryWeiXinContactors(queryDomainByCommand(id));
	}

	@Override
	public List<String> querySmsContactors(String id) {
		return super.querySmsContactors(queryDomainByCommand(id));

	}

}
