package com.dianping.cat.report.alert.app;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.alarm.spi.AlertType;
import com.dianping.cat.alarm.spi.receiver.ProjectContactor;
import com.dianping.cat.command.entity.Command;
import com.dianping.cat.config.app.AppCommandConfigManager;

public class AppContactor extends ProjectContactor {

	@Inject
	protected AppCommandConfigManager m_appConfigManager;

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
	public List<String> queryDXContactors(String id) {
		return super.queryDXContactors(queryDomainByCommand(id));
	}

	@Override
	public List<String> queryEmailContactors(String id) {
		return super.queryEmailContactors(queryDomainByCommand(id));
	}

	@Override
	public List<String> querySmsContactors(String id) {
		return super.querySmsContactors(queryDomainByCommand(id));

	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		return super.queryWeiXinContactors(queryDomainByCommand(id));
	}

}
