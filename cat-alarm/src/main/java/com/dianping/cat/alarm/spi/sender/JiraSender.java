package com.dianping.cat.alarm.spi.sender;

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.dianping.cat.alarm.sender.entity.Par;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.util.JiraHelper;
import com.dianping.cat.alarm.spi.sender.util.JiraIssue;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Jira Software 发送
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 3.3.3
 */
public class JiraSender extends AbstractSender {

	public static final String ID = AlertChannel.JIRA.getName();

	@Override
	public String getId() {
		return ID;
	}

	@Override
	public boolean send(SendMessageEntity message) {
		com.dianping.cat.alarm.sender.entity.Sender sender = querySender();
		boolean result = false;

		String url = sender.getUrl();
		try {
			List<String> receivers = message.getReceivers();
			for (String receiver : receivers) {
				if (receiver == null) {
					continue;
				}

				String projectKey = m_senderConfigManager.getParValue(sender, "projectKey");
				String summary = m_senderConfigManager.getParValue(sender, "summary");
				String description = m_senderConfigManager.getParValue(sender, "description");
				JiraIssue issue = new JiraIssue(projectKey, summary, description);
				issue.setAssigneeName(receiver);

				String issueType = m_senderConfigManager.getParValue(sender, "issueType");
				issue.setIssueType(issueType);

				String parsedComponents = m_senderConfigManager.getParValue(sender, "components");
				List<String> components = Lists.newArrayList();
				for (String component : parsedComponents.split(",")) {
					components.add(component);
				}
				issue.setComponents(components);

				String parsedFixVersionNames = m_senderConfigManager.getParValue(sender, "fixVersionNames");
				List<String> fixVersionNames = Lists.newArrayList();
				for (String fixVersionName : parsedFixVersionNames.split(",")) {
					fixVersionNames.add(fixVersionName);
				}
				issue.setFixVersionNames(fixVersionNames);

				String reporterName = m_senderConfigManager.getParValue(sender, "reporterName");
				issue.setReporterName(reporterName);

				Map<String, String> customFields = parseCustomFields(sender);
				for (Map.Entry<String, String> field : customFields.entrySet()) {
					issue.addCustomFields(field.getKey(), field.getValue());
				}

				String[] receiverArr = receiver.split(":");
				String token = receiverArr.length > 1 ? receiverArr[0] : receiver;
				JiraHelper jiraHelper = new JiraHelper(url, token);
				m_logger.info("Jira send to [" + url + "]");

				BasicIssue createdIssue = jiraHelper.createIssue(issue);
				m_logger.info("Jira created success, issue key: " + createdIssue.getId());
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
		return result;
	}

	private Map<String, String> parseCustomFields(com.dianping.cat.alarm.sender.entity.Sender sender) {
		Map<String, String> customFields = Maps.newHashMap();
		List<Par> pars = sender.getPars();
		for (Par par : pars) {
			int index = par.getId().indexOf("customfield_");
			if (index>= 0) {
				String[] parts = par.getId().split("=");
				if (parts.length == 2) {
					customFields.put(parts[0], parts[1]);
				} else {
					throw new IllegalArgumentException("Invalid format. Expected 'key=value', got: " + par.getId());
				}
			}
		}
		return customFields;
	}
}
