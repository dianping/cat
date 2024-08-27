package com.dianping.cat;

import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.dianping.cat.alarm.spi.sender.util.JiraHelper;
import com.dianping.cat.alarm.spi.sender.util.JiraIssue;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * Jira Software Client Integration Test
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 2.4.13
 */
public class JiraClientIntTest {

	public static void main(String[] args) {
		String projectKey = "ARCHITEC";
		String summary = "测试标题";
		String description = "测试描述";
		String issueType = "故障";
		List<String> components = Lists.newArrayList("架构");
		List<String> fixVersionNames = Lists.newArrayList("待定");
		String assigneeName = "guoyuanlu";

		JiraIssue issue = new JiraIssue(projectKey, summary, description);
		issue.setIssueType(issueType);
		issue.setComponents(components);
		issue.setFixVersionNames(fixVersionNames);
		issue.setAssigneeName(assigneeName);
		issue.addCustomFields("customfield_11201", "ROI");
		JiraHelper jiraHelper = new JiraHelper("", "");
		try {
			BasicIssue createdIssue = jiraHelper.createIssue(issue);
			System.out.println("Created issue with key: " + createdIssue.getKey());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
