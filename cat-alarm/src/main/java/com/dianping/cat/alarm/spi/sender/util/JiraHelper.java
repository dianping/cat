package com.dianping.cat.alarm.spi.sender.util;

import com.atlassian.httpclient.api.Request;
import com.atlassian.jira.rest.client.api.AuthenticationHandler;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.OptionalIterable;
import com.atlassian.jira.rest.client.api.domain.BasicComponent;
import com.atlassian.jira.rest.client.api.domain.BasicIssue;
import com.atlassian.jira.rest.client.api.domain.IssueType;
import com.atlassian.jira.rest.client.api.domain.Project;
import com.atlassian.jira.rest.client.api.domain.input.FieldInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInput;
import com.atlassian.jira.rest.client.api.domain.input.IssueInputBuilder;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Jira Software Helper
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 3.3.3
 */
public class JiraHelper {

	private final String address;

	private String username;

	private String password;

	private String token;

	@Deprecated
	public JiraHelper(String address, String username, String password) {
		this.address = address;
		this.username = username;
		this.password = password;
	}

	public JiraHelper(String address, String token) {
		this.address = address;
		this.token = token;
	}

	public BasicIssue createIssue(JiraIssue issue) throws ExecutionException, InterruptedException, IOException {
		try (JiraRestClient restClient = auth()) {
			IssueInputBuilder builder= new IssueInputBuilder()
				.setProjectKey(issue.getProjectKey())
				.setSummary(issue.getSummary())
				.setDescription(issue.getDescription());

			if (issue.getIssueType() != null) {
				IssueType issueType = getIssueType(restClient, issue.getProjectKey(), issue.getIssueType());
				builder.setIssueType(issueType);
			}

			if (issue.getComponents() != null) {
				List<BasicComponent> components = getComponent(restClient, issue.getProjectKey(), issue.getComponents());
				builder.setComponents(components);
			}

			if (issue.getFixVersionNames() != null) {
				builder.setFixVersionsNames(Lists.newArrayList(issue.getFixVersionNames()));
			}

			if (issue.getAssigneeName() != null) {
				builder.setAssigneeName(issue.getAssigneeName());
			}

			if (issue.getReporterName() != null) {
				builder.setReporterName(issue.getReporterName());
			}

			IssueInput issueInput = builder.build();
			for (Map.Entry<String, Object> entry : issue.getCustomFields().entrySet()) {
				FieldInput fieldInput = new FieldInput(entry.getKey(), entry.getValue());
				issueInput.getFields().put(fieldInput.getId(), fieldInput);
			}
			return restClient.getIssueClient().createIssue(issueInput).get();
		}
	}

	public Project getProject(String projectKey) throws IOException {
		try (JiraRestClient restClient = auth()) {
			return restClient.getProjectClient().getProject(projectKey).claim();
		}
	}

	private JiraRestClient auth() {
		URI uri = URI.create(address);
		JiraRestClientFactory factory = new AsynchronousJiraRestClientFactory();
		if (this.token != null) {
			return factory.createWithAuthenticationHandler(uri, new AuthenticationHandler() {

				@Override
				public void configure(Request.Builder builder) {
					builder.setHeader("Authorization", "Bearer " + token);
				}
			});
		} else {
			return factory.createWithBasicHttpAuthentication(uri, username, password);
		}
	}

	private Project getProject(JiraRestClient restClient, String projectKey) {
		return restClient.getProjectClient().getProject(projectKey).claim();
	}

	private IssueType getIssueType(JiraRestClient restClient, String projectKey, String issueTypeName) {
		if (issueTypeName == null || issueTypeName.isEmpty()) {
			throw new RuntimeException("Issue type is required.");
		}
		OptionalIterable<IssueType> issueTypes = getProject(restClient, projectKey).getIssueTypes();
		for (String name : issueTypeName.split(",")) {
			for (IssueType issueType : issueTypes) {
				if (issueType.getName().equals(name)) {
					return issueType;
				}
			}
		}
		throw new RuntimeException("Issue type '" + issueTypeName + "' is not found.");
	}

	private List<BasicComponent> getComponent(JiraRestClient restClient, String projectKey, List<String> componentNames) {
		if (componentNames == null || componentNames.isEmpty()) {
			throw new RuntimeException("Component name is required.");
		}
		List<BasicComponent> components = new ArrayList<>();
		Iterable<BasicComponent> basicComponents = getProject(restClient, projectKey).getComponents();
		for (BasicComponent component : basicComponents) {
			for (String componentName : componentNames) {
				if (component.getName().equals(componentName)) {
					components.add(component);
				}
			}
		}
		return components;
	}
}
