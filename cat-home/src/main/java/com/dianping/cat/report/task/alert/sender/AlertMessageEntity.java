package com.dianping.cat.report.task.alert.sender;

import java.util.List;

public class AlertMessageEntity {
	private String m_group;

	private String m_content;

	private String m_title;

	private List<String> m_receivers;

	public AlertMessageEntity(String group, String title, String content, List<String> receivers) {
		m_group = group;
		m_title = title;
		m_content = content;
		m_receivers = receivers;
	}

	public String getContent() {
		return m_content;
	}

	public String getDomain() {
		return m_group;
	}

	public List<String> getReceivers() {
		return m_receivers;
	}

	public String getReceiverString() {
		StringBuilder builder = new StringBuilder(100);

		for (String receiver : m_receivers) {
			builder.append(receiver).append(",");
		}

		String tmpResult = builder.toString();
		if (tmpResult.endsWith(",")) {
			return tmpResult.substring(0, tmpResult.length() - 1);
		} else {
			return tmpResult;
		}
	}

	public String getTitle() {
		return m_title;
	}

	public void setContent(String content) {
		this.m_content = content;
	}

	public void setDomain(String domain) {
		this.m_group = domain;
	}

	public void setReceivers(List<String> receivers) {
		this.m_receivers = receivers;
	}

	public void setTitle(String title) {
		this.m_title = title;
	}

	@Override
	public String toString() {
		return "title: " + m_title + " content: " + m_content + " receiver: " + getReceiverString();
	}

}