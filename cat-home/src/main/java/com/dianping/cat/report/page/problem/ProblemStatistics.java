package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.dianping.cat.consumer.problem.model.entity.Entry;

public class ProblemStatistics {
	private int m_count;

	private String m_type;

	private Map<String, StatusStatistics> m_status = new HashMap<String, StatusStatistics>();

	public ProblemStatistics(Entry entry) {
		m_type = entry.getType();
		add(entry);
	}

	public void add(Entry entry) {
		m_count++;
		String status = entry.getStatus();
		StatusStatistics statusStatistics = m_status.get(status);

		if (statusStatistics == null) {
			m_status.put(status, new StatusStatistics(entry));
		} else {
			statusStatistics.add(entry);
		}
	}

	public int getCount() {
		return m_count;
	}

	public ProblemStatistics setCount(int count) {
		m_count = count;
		return this;
	}

	public String getType() {
		return m_type;
	}

	public ProblemStatistics setType(String type) {
		m_type = type;
		return this;
	}

	public Map<String, StatusStatistics> getStatus() {
		return m_status;
	}

	public void setStatus(Map<String, StatusStatistics> status) {
		m_status = status;
	}

	public static class StatusStatistics {
		private String m_status;

		private int m_count;

		private List<String> m_links = new ArrayList<String>();

		private static int s_maxLinkSize = 5;

		public StatusStatistics(Entry entry) {
			m_status = entry.getStatus();
			m_count = 1;
			m_links.add(entry.getMessageId());
		}

		public void add(Entry entry) {
			m_count++;
			if (m_links.size() <= s_maxLinkSize) {
				m_links.add(entry.getMessageId());
			}
		}

		public String getStatus() {
			return m_status;
		}

		public StatusStatistics setStatus(String status) {
			m_status = status;
			return this;
		}

		public int getCount() {
			return m_count;
		}

		public StatusStatistics setCount(int count) {
			m_count = count;
			return this;
		}

		public List<String> getLinks() {
			return m_links;
		}

		public StatusStatistics setLinks(List<String> links) {
			m_links = links;
			return this;
		}

		public void addLinks(String link) {
			m_links.add(link);
		}
	}
}
