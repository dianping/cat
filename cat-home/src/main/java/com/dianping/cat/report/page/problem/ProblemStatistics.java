package com.dianping.cat.report.page.problem;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.dianping.cat.consumer.problem.ProblemType;
import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entry;
import com.dianping.cat.consumer.problem.model.entity.JavaThread;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.entity.ProblemReport;
import com.dianping.cat.consumer.problem.model.entity.Segment;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.MapUtils;

public class ProblemStatistics extends BaseVisitor {

	private Map<String, TypeStatistics> m_status = new TreeMap<String, TypeStatistics>();

	private boolean m_allIp = false;

	private String m_ip = "";

	private List<String> m_ips;

	private int m_sqlThreshold = 100;

	private int m_urlThreshold = 1000;

	private int m_serviceThreshold = 50;

	private List<Duration> getDurationsByType(String type, Entry entry) {
		List<Duration> durations = new ArrayList<Duration>();
		if (ProblemType.LONG_URL.getName().equals(type)) {
			for (java.util.Map.Entry<Integer, Duration> temp : entry.getDurations().entrySet()) {
				if (temp.getKey() >= m_urlThreshold) {
					durations.add(temp.getValue());
				}
			}
		} else if (ProblemType.LONG_SQL.getName().equals(type)) {
			for (java.util.Map.Entry<Integer, Duration> temp : entry.getDurations().entrySet()) {
				if (temp.getKey() >= m_sqlThreshold) {
					durations.add(temp.getValue());
				}
			}
		} else if (ProblemType.LONG_SERVICE.getName().equals(type)) {
			for (java.util.Map.Entry<Integer, Duration> temp : entry.getDurations().entrySet()) {
				if (temp.getKey() >= m_serviceThreshold) {
					durations.add(temp.getValue());
				}
			}
		} else {
			durations.add(entry.getDurations().get(0));
		}
		return durations;
	}

	public List<String> getIps() {
		return m_ips;
	}

	public Map<String, TypeStatistics> getStatus() {
		return m_status;
	}

	public ProblemStatistics setAllIp(boolean allIp) {
		m_allIp = allIp;
		return this;
	}

	public ProblemStatistics setIp(String ip) {
		m_ip = ip;
		return this;
	}

	public void setIps(List<String> ips) {
		m_ips = ips;
	}

	public ProblemStatistics setSqlThreshold(int sqlThreshold) {
		m_sqlThreshold = sqlThreshold;
		return this;
	}

	public ProblemStatistics setUrlThreshold(int urlThreshold) {
		m_urlThreshold = urlThreshold;
		return this;
	}

	public ProblemStatistics setServiceThreshold(int serviceThreshold) {
		m_serviceThreshold = serviceThreshold;
		return this;
	}

	private void statisticsDuration(Entry entry) {
		String type = entry.getType();
		String status = entry.getStatus();
		List<Duration> durations = getDurationsByType(type, entry);
		for (Duration duration : durations) {
			TypeStatistics statusValue = m_status.get(type);

			if (statusValue == null) {
				statusValue = new TypeStatistics(type);
				m_status.put(type, statusValue);
			}
			statusValue.statics(status, duration);
		}
	}

	@Override
	public void visitDuration(Duration duration) {
		super.visitDuration(duration);
	}

	@Override
	public void visitEntry(Entry entry) {
		super.visitEntry(entry);
	}

	@Override
	public void visitMachine(Machine machine) {
		if (m_allIp == true || m_ip.equals(machine.getIp())) {
			List<Entry> entries = machine.getEntries();
			for (Entry entry : entries) {
				statisticsDuration(entry);
			}
		}
		super.visitMachine(machine);

	}

	@Override
	public void visitProblemReport(ProblemReport problemReport) {
		super.visitProblemReport(problemReport);
	}

	@Override
	public void visitSegment(Segment segment) {
		super.visitSegment(segment);
	}

	@Override
	public void visitThread(JavaThread thread) {
		super.visitThread(thread);
	}

	public static class StatusStatistics {
		private int m_count;

		private List<String> m_links = new ArrayList<String>();

		private String m_status;

		private StatusStatistics(String status) {
			m_status = status;
		}

		public void addLinks(String link) {
			m_links.add(link);
		}

		public int getCount() {
			return m_count;
		}

		public List<String> getLinks() {
			return m_links;
		}

		public String getStatus() {
			return m_status;
		}

		public StatusStatistics setCount(int count) {
			m_count = count;
			return this;
		}

		public StatusStatistics setLinks(List<String> links) {
			m_links = links;
			return this;
		}

		public StatusStatistics setStatus(String status) {
			m_status = status;
			return this;
		}

		public void statics(Duration duration) {
			m_count += duration.getCount();
			if (m_links.size() < 60) {
				m_links.addAll(duration.getMessages());
				if (m_links.size() > 60) {
					m_links = m_links.subList(0, 60);
				}
			}
		}
	}

	public static class TypeStatistics {
		private int m_count;

		private Map<String, StatusStatistics> m_status = new LinkedHashMap<String, StatusStatistics>();

		private String m_type;

		public TypeStatistics(String type) {
			m_type = type;
		}

		public int getCount() {
			return m_count;
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

		public String getType() {
			return m_type;
		}

		public TypeStatistics setCount(int count) {
			m_count = count;
			return this;
		}

		public void setStatus(Map<String, StatusStatistics> status) {
			m_status = status;
		}

		public TypeStatistics setType(String type) {
			m_type = type;
			return this;
		}

		public void statics(String status, Duration duration) {
			if (duration != null) {
				m_count += duration.getCount();

				StatusStatistics value = m_status.get(status);
				if (value == null) {
					value = new StatusStatistics(status);
					m_status.put(status, value);
				}
				value.statics(duration);
			}
		}
	}
}
