package com.dianping.cat.report.task.alert.sender.receiver;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.config.app.AppConfigManager;
import com.dianping.cat.configuration.app.entity.Command;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.report.task.alert.AlertType;
import com.dianping.cat.service.ProjectService;
import com.dianping.cat.system.config.AlertConfigManager;

public class AppContactor extends DefaultContactor implements Contactor {

	@Inject
	protected ProjectService m_projectService;

	@Inject
	protected AppConfigManager m_appConfigManager;

	@Inject
	protected AlertConfigManager m_alertConfigManager;

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
		List<String> mailReceivers = new ArrayList<String>();

		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());
		
		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));

			Project project = m_projectService.findByDomain(queryDomainByCommand(id));
			if (project != null) {
				mailReceivers.addAll(split(project.getEmail()));
			}
			return mailReceivers;
		}
	}

	@Override
	public List<String> queryWeiXinContactors(String id) {
		List<String> weixinReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return weixinReceivers;
		} else {
			weixinReceivers.addAll(buildDefaultWeixinReceivers(receiver));

			Project project = m_projectService.findByDomain(queryDomainByCommand(id));

			if (project != null) {
				weixinReceivers.addAll(split(project.getEmail()));
			}
			return weixinReceivers;
		}
	}

	@Override
	public List<String> querySmsContactors(String id) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_alertConfigManager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));

			Project project = m_projectService.findByDomain(queryDomainByCommand(id));

			if (project != null) {
				smsReceivers.addAll(split(project.getPhone()));
			}
			return smsReceivers;
		}
	}

}
