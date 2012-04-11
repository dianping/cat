package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.helper.MapUtils;

public class ProblemStatistics {

	private Map<String, TypeStatistics> m_status = new TreeMap<String, TypeStatistics>();

	private String m_groupName;

	private String m_threadId;

	private int m_threshold = ONE_SECOND;

	private static final int ONE_SECOND = 1000;

	public String getSubTitle() {
		StringBuilder sb = new StringBuilder();
		if (isEmpty(m_threadId) && isEmpty(m_groupName)) {
			return "All Thread Groups";
		} else if (!isEmpty(m_groupName) && isEmpty(m_threadId)) {
			return "All Threads in Group:" + m_groupName;
		} else if (!isEmpty(m_groupName) && !isEmpty(m_threadId)) {
			return "Thread :" + m_threadId;
		}
		return sb.toString();
	}

	public String getUrl() {
		StringBuilder sb = new StringBuilder();
		if (!isEmpty(m_groupName)) {
			sb.append("&group=").append(m_groupName);
		}
		if (!isEmpty(m_threadId)) {
			sb.append("&thread=").append(m_threadId);
		}
		return sb.toString();
	}

	public ProblemStatistics displayAllIp(ProblemReport report) {
		if (report == null) {
			return null;
		}
		for (Machine machine : report.getMachines().values()) {
			//All Ip
			Map<String, JavaThread> threads = machine.getThreads();
			for (JavaThread thread : threads.values()) {
				for (Segment segment : thread.getSegments().values()) {
					if (segment == null) {
						continue;
					}
					List<Entry> entries = segment.getEntries();
					statisticsEntries(entries, m_threshold);
				}
			}
		}
		return this;
	}

	public ProblemStatistics displayByIp(ProblemReport report, Model model, Payload payload) {
		m_threshold = payload.getLongTime();

		if (report == null) {
			return null;
		}
		Machine machine = report.getMachines().get(model.getIpAddress());

		if (machine == null) {
			return null;
		}
		// All Level
		Map<String, JavaThread> threads = machine.getThreads();
		for (JavaThread thread : threads.values()) {
			for (Segment segment : thread.getSegments().values()) {
				if (segment == null) {
					continue;
				}
				List<Entry> entries = segment.getEntries();
				statisticsEntries(entries, m_threshold);
			}
		}

		return this;
	}

	public ProblemStatistics displayByGroupOrThread(ProblemReport report, Model model) {
		Machine machine = report.getMachines().get(model.getIpAddress());

		if (machine == null) {
			return null;
		}

		m_groupName = model.getGroupName();
		m_threadId = model.getThreadId();

		if (isEmpty(m_threadId) && isEmpty(m_groupName)) {
			// Min Level
			Map<String, JavaThread> threads = machine.getThreads();
			for (JavaThread thread : threads.values()) {
				Segment segment = thread.getSegments().get(model.getCurrentMinute());
				if (segment == null) {
					continue;
				}
				List<Entry> entries = segment.getEntries();
				statisticsEntries(entries, ONE_SECOND);
			}

		} else if (!isEmpty(m_groupName) && isEmpty(m_threadId)) {
			// Group Level Show
			Map<String, JavaThread> threads = machine.getThreads();
			for (JavaThread thread : threads.values()) {
				if (thread.getGroupName().equals(m_groupName)) {
					Segment segment = thread.getSegments().get(model.getCurrentMinute());
					if (segment == null) {
						continue;
					}
					List<Entry> entries = segment.getEntries();
					statisticsEntries(entries, ONE_SECOND);
				}
			}

		} else if (!isEmpty(m_groupName) && !isEmpty(m_threadId)) {
			// Thread Level Show
			JavaThread thread = machine.getThreads().get(model.getThreadId());
			if (thread == null) {
				return null;
			}
			Segment segment = thread.getSegments().get(model.getCurrentMinute());
			if (segment == null) {
				return null;
			}
			List<Entry> entries = segment.getEntries();
			statisticsEntries(entries, ONE_SECOND);
		}

		return this;
	}

	private void statisticsEntries(List<Entry> entries, int longTime) {
		for (Entry entry : entries) {
			String type = entry.getType();
			TypeStatistics staticstics = m_status.get(type);

			if (type.equals("long-url") && entry.getDuration() < longTime) {
				// Skip the duration which duration less than longTime
				continue;
			}

			if (staticstics != null) {
				staticstics.add(entry, m_groupName, m_threadId);
			} else {
				m_status.put(type, new TypeStatistics(entry, m_groupName, m_threadId));
			}
		}
	}

	private boolean isEmpty(String str) {
		if (str == null || str.length() == 0) {
			return true;
		}
		return false;
	}

	public Map<String, TypeStatistics> getStatus() {
		return m_status;
	}

	public static class TypeStatistics {
		private int m_count;

		private String m_type;

		private Map<String, StatusStatistics> m_status = new LinkedHashMap<String, StatusStatistics>();

		public TypeStatistics(Entry entry, String groupName, String threadName) {
			m_type = entry.getType();
			add(entry, groupName, threadName);
		}

		public void add(Entry entry, String groupName, String threadId) {
			m_count++;
			String status = entry.getStatus();
			StatusStatistics statusStatistics = m_status.get(status);

			if (statusStatistics == null) {
				m_status.put(status, new StatusStatistics(entry, groupName, threadId));
			} else {
				statusStatistics.add(entry, groupName, threadId);
			}
		}

		public int getCount() {
			return m_count;
		}

		public TypeStatistics setCount(int count) {
			m_count = count;
			return this;
		}

		public String getType() {
			return m_type;
		}

		public TypeStatistics setType(String type) {
			m_type = type;
			return this;
		}

		public Map<String, StatusStatistics> getStatus() {
			Map<String, StatusStatistics> result = MapUtils.sortMap(m_status,
			      new Comparator<java.util.Map.Entry<String, StatusStatistics>>() {
				      @Override
				      public int compare(java.util.Map.Entry<String, StatusStatistics> o1,
				            java.util.Map.Entry<String, StatusStatistics> o2) {
					      return o2.getValue().getCount() - o1.getValue().getCount();
				      }
			      });
			return result;
		}

		public void setStatus(Map<String, StatusStatistics> status) {
			m_status = status;
		}
	}

	public static class StatusStatistics {
		private String m_status;

		private int m_count;

		private List<String> m_links = new ArrayList<String>();

		private static int s_maxLinkSize = 20;

		public StatusStatistics(Entry entry, String groupName, String threadId) {
			m_status = entry.getStatus();
			add(entry, groupName, threadId);
		}

		public void add(Entry entry, String groupName, String threadId) {
			if (m_links.size() < s_maxLinkSize) {
				m_links.add(entry.getMessageId());
			}
			m_count++;
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
