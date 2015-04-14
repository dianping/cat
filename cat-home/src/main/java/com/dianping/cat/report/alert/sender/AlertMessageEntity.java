package com.dianping.cat.report.alert.sender;

import java.util.List;

public class AlertMessageEntity {
	private String m_group;

	private String m_title;

	private String m_type;

	private String m_content;

	private List<String> m_receivers;

	public AlertMessageEntity(String group, String title, String type, String content, List<String> receivers) {
		m_group = group;
		m_title = title;
		m_type = type;
		m_content = content;
		m_receivers = receivers;
	}
	
	public String getContent() {
		return m_content;
	}

	public String getGroup() {
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

	public String getType() {
		return m_type;
	}

	public void setContent(String content) {
   	m_content = content;
   }

	@Override
	public String toString() {
		return "title: " + m_title + " content: " + m_content + " type: " + m_type + " receiver: " + getReceiverString();
	}

}