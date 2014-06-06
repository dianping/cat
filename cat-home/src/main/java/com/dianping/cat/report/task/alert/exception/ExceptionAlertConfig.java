package com.dianping.cat.report.task.alert.exception;

import java.util.ArrayList;
import java.util.List;

import com.dianping.cat.core.dal.Project;
import com.dianping.cat.home.alert.config.entity.Receiver;
import com.dianping.cat.report.task.alert.BaseAlertConfig;

public class ExceptionAlertConfig extends BaseAlertConfig {

	private String m_id = "exception";

	public List<String> buildMailReceivers(Project project) {
		List<String> mailReceivers = new ArrayList<String>();
		Receiver receiver = m_manager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return mailReceivers;
		} else {
			mailReceivers.addAll(buildDefaultMailReceivers(receiver));
			mailReceivers.addAll(buildProjectMailReceivers(project));

			return mailReceivers;
		}
	}

	private List<String> buildProjectMailReceivers(Project project) {
		return split(project.getEmail());
	}
	
	public List<String> buildSMSReceivers(Project project) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_manager.getReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return smsReceivers;
		} else {
			smsReceivers.addAll(buildDefaultSMSReceivers(receiver));
			smsReceivers.addAll(buildProjectSMSReceivers(project));

			return smsReceivers;
		}
	}
	
	private List<String> buildProjectSMSReceivers(Project project) {
		return split(project.getPhone());
	}

	public String getId() {
		return m_id;
	}

}
