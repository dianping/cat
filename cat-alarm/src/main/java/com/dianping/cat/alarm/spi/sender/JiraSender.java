package com.dianping.cat.alarm.spi.sender;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.dianping.cat.alarm.spi.AlertChannel;
import com.dianping.cat.alarm.spi.sender.util.JiraHelper;
import com.dianping.cat.alarm.spi.sender.util.JiraIssue;
import com.dianping.cat.core.dal.Project;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.site.lookup.util.StringUtils;

import java.util.Collections;
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
		if (message.getTitle().contains("系统已恢复")) { // 不需要录入已恢复的记录
			return true;
		}

		com.dianping.cat.alarm.sender.entity.Sender sender = querySender();
		boolean result = false;

		String url = sender.getUrl();
		try {
			List<String> receivers = message.getReceivers();
			for (String receiver : receivers) {
				if (receiver == null) {
					continue;
				}

				Project project = message.getProject();

				String projectKey = this.getParameterValue(receiver, "projectKey");
				if (StringUtils.isEmpty(projectKey)) {
					projectKey = project.getKey();
					if (StringUtils.isEmpty(projectKey)) {
						throw new RuntimeException("Jira sender 'projectKey' is required");
					}
				}

				String summary = message.getTitle();
				String description = message.getContent();

				if (StringUtils.isNotEmpty(description)) {
					description = description.replaceAll("<br/>", "\n");
				}
				description += "\n\n[\uD83D\uDD27 告警规则|" + message.getSettingsLink() + "]";
				description += "    [\uD83D\uDD14 查看告警|" + message.getViewLink() + "]";

				JiraIssue issue = new JiraIssue(projectKey, summary, description);

				String assigneeName = this.getParameterValue(receiver, "assigneeName");
				if (StringUtils.isNotEmpty(assigneeName)) {
					issue.setAssigneeName(assigneeName);
				} else {
					issue.setAssigneeName(project.getAssigner());
				}

				String reporterName = this.getParameterValue(receiver, "reporterName");
				if (StringUtils.isNotEmpty(reporterName)) {
					issue.setReporterName(reporterName);
				} else {
					issue.setReporterName(project.getAssigner());
				}

				String issueType = this.getParameterValue(receiver, "issueType");
				if (StringUtils.isNotEmpty(issueType)) {
					issue.setIssueType(issueType);
				}

				String parsedComponents = this.getParameterValue(receiver, "components");
				if (StringUtils.isNotEmpty(parsedComponents)) {
					List<String> components = Lists.newArrayList();
					Collections.addAll(components, parsedComponents.split(","));
					issue.setComponents(components);
				}

				String parsedFixVersionNames = this.getParameterValue(receiver,"fixVersionNames");
				if (StringUtils.isNotEmpty(parsedFixVersionNames)) {
					List<String> fixVersionNames = Lists.newArrayList();
					Collections.addAll(fixVersionNames, parsedFixVersionNames.split(","));
					issue.setFixVersionNames(fixVersionNames);
				}

				Map<String, String> customFields = parseCustomFields(receiver);
				for (Map.Entry<String, String> field : customFields.entrySet()) {
					issue.addCustomFields(field.getKey(), field.getValue());
				}

				String token = m_senderConfigManager.getParValue(sender, "reporter_token");
				JiraHelper jiraHelper = new JiraHelper(url, token);
				m_logger.info("Jira send to [" + url + "]");

				BasicIssue createdIssue = jiraHelper.createIssue(issue);
				m_logger.info("Jira created success, issue key: " + createdIssue.getKey());
			}
		} catch (Exception e) {
			m_logger.error(e.getMessage(), e);
		}
		return result;
	}

	private Map<String, String> parseCustomFields(String text) {
		Map<String, String> customFields = Maps.newHashMap();
		String[] pairs = text.split("&");
		for (String pair : pairs) {
			int eqIndex = pair.indexOf('=');
			if (eqIndex >= 0) {
				String key = pair.substring(0, eqIndex).trim();
				if (key.startsWith("customfield_")) {
					String value = pair.substring(eqIndex + 1).trim();
					customFields.put(key, value);
				}
			}
		}
		return customFields;
	}

	private String getParameterValue(String text, String matchKey) {
		String[] pairs = text.split("&");
		for (String pair : pairs) {
			int eqIndex = pair.indexOf('=');
			if (eqIndex >= 0) {
				String key = pair.substring(0, eqIndex).trim();
				String value = pair.substring(eqIndex + 1).trim();
				if (key.equals(matchKey)) {
					return value;
				}
			}
		}
		return null;
	}
}
