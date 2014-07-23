package com.dianping.cat.report.task.alert.exception;

import java.util.ArrayList;
import java.util.Arrays;
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

	public List<String> buildWeiXinReceivers(Project project) {
		StringBuilder builder = new StringBuilder();
		Receiver receiver = m_manager.queryReceiverById(getId());

		if (receiver != null && !receiver.isEnable()) {
			return null;
		} else {
			builder.append(buildDefaultWeixinReceivers(receiver));
			builder.append(project.getEmail());

			String result = builder.toString();

			if (result.endsWith(",")) {
				result = result.substring(0, result.length() - 1);
			}

			return Arrays.asList(result.split(","));
		}
	}

	private String buildDefaultWeixinReceivers(Receiver receiver) {
		StringBuilder builder = new StringBuilder();

		if (receiver != null) {
			for (String weixin : receiver.getWeixins()) {
				builder.append(weixin + ",");
			}
		}

		return builder.toString();
	}

	@Override
	public String buildMailTitle(String domain, String metricName) {
		StringBuilder sb = new StringBuilder();

		sb.append("[CAT异常告警] [项目: ").append(domain).append("]");
		return sb.toString();
	}

	private List<String> buildProjectMailReceivers(Project project) {
		return split(project.getEmail());
	}

	public List<String> buildSMSReceivers(Project project) {
		List<String> smsReceivers = new ArrayList<String>();
		Receiver receiver = m_manager.queryReceiverById(getId());

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
