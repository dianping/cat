package com.dianping.cat.alarm.spi.sender.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Jira Software Issue Model
 *
 * @author <a href="mailto:shiyindaxiaojie@gmail.com">gyl</a>
 * @since 3.3.3
 */
public class JiraIssue {

	/**
	 * Project Key
	 */
	private String projectKey;

	/**
	 * Title
	 */
	private String summary;

	/**
	 * Description
	 */
	private String description;

	/**
	 * Issue Type
	 */
	private String issueType;

	/**
	 * Components
	 */
	private List<String> components;

	/**
	 * Fix Versions Names
	 */
	private List<String> fixVersionNames;

	/**
	 * Reporter Name
	 */
	private String reporterName;

	/**
	 * Assignee Name
	 */
	private String assigneeName;

	/**
	 * Custom Fields
	 */
	private final Map<String, Object> customFields = new HashMap<>();;

	public JiraIssue(String projectKey, String summary, String description) {
		this.projectKey = projectKey;
		this.summary = summary;
		this.description = description;
	}

	public String getProjectKey() {
		return projectKey;
	}

	public void setProjectKey(String projectKey) {
		this.projectKey = projectKey;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getIssueType() {
		return issueType;
	}

	public void setIssueType(String issueType) {
		this.issueType = issueType;
	}

	public List<String> getComponents() {
		return components;
	}

	public void setComponents(List<String> components) {
		this.components = components;
	}

	public List<String> getFixVersionNames() {
		return fixVersionNames;
	}

	public void setFixVersionNames(List<String> fixVersionNames) {
		this.fixVersionNames = fixVersionNames;
	}

	public String getReporterName() {
		return reporterName;
	}

	public void setReporterName(String reporterName) {
		this.reporterName = reporterName;
	}

	public String getAssigneeName() {
		return assigneeName;
	}

	public void setAssigneeName(String assigneeName) {
		this.assigneeName = assigneeName;
	}

	public void addCustomFields(String id, Object value) {
		this.customFields.put(id, value);
	}

	public Map<String, Object> getCustomFields() {
		return customFields;
	}
}
