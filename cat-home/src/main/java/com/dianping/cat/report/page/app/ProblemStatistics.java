package com.dianping.cat.report.page.app;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.util.StringUtils;
import org.unidal.helper.Splitters;

import com.dianping.cat.consumer.problem.model.entity.Duration;
import com.dianping.cat.consumer.problem.model.entity.Entity;
import com.dianping.cat.consumer.problem.model.entity.Machine;
import com.dianping.cat.consumer.problem.model.transform.BaseVisitor;
import com.dianping.cat.helper.SortHelper;

public class ProblemStatistics extends BaseVisitor {

	private Map<String, TypeStatistics> m_types = new TreeMap<String, TypeStatistics>();

	private String m_type = "error";

	private String m_status;

	private List<String> m_appVersions;

	private List<String> m_platformVersions;

	private List<String> m_modules;

	private List<String> m_levels;

	private boolean checkFlag(List<String> myFields, String field) {
		if (myFields == null || myFields.isEmpty() || !myFields.isEmpty() && myFields.contains(field)) {
			return true;
		} else {
			return false;
		}
	}

	private boolean checkFlag(String myField, String field) {
		if (StringUtils.isEmpty(myField) || StringUtils.isNotEmpty(myField) && myField.equals(field)) {
			return true;
		} else {
			return false;
		}
	}

	public List<String> getAppVersions() {
		return m_appVersions;
	}

	public List<String> getLevels() {
		return m_levels;
	}

	public List<String> getModules() {
		return m_modules;
	}

	public List<String> getPlatformVersions() {
		return m_platformVersions;
	}

	public String getType() {
		return m_type;
	}

	public Map<String, TypeStatistics> getTypes() {
		return m_types;
	}

	public void setAppVersions(List<String> appVersions) {
		m_appVersions = appVersions;
	}

	public void setLevels(List<String> levels) {
		m_levels = levels;
	}

	public void setModules(List<String> modules) {
		m_modules = modules;
	}

	public void setPlatformVersions(List<String> platformVersions) {
		m_platformVersions = platformVersions;
	}

	public void setStatus(String status) {
		m_status = status;
	}

	public void setType(String type) {
		m_type = type;
	}

	public void setTypes(Map<String, TypeStatistics> types) {
		m_types = types;
	}

	private void statisticsDuration(Entity entity) {
		String type = entity.getType();
		String status = entity.getStatus();
		boolean flag = checkFlag(m_type, type) && checkFlag(m_status, status);

		if (flag) {
			Map<Integer, Duration> durations = entity.getDurations();

			for (Map.Entry<Integer, Duration> e : durations.entrySet()) {
				TypeStatistics statusValue = m_types.get(type);

				if (statusValue == null) {
					statusValue = new TypeStatistics(type);
					m_types.put(type, statusValue);
				}
				statusValue.statics(status, e.getValue());
			}
		}
	}

	@Override
	public void visitMachine(Machine machine) {
		List<String> names = Splitters.by(":").split(machine.getIp());
		String appVersion = names.get(0);
		String platformVersion = names.get(1);
		String module = names.get(2);
		String level = names.get(3);
		boolean flag = checkFlag(m_appVersions, appVersion) && checkFlag(m_platformVersions, platformVersion)
		      && checkFlag(m_modules, module) && checkFlag(m_levels, level);

		if (flag) {
			for (Entity problem : machine.getEntities().values()) {
				statisticsDuration(problem);
			}
		}
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
			Map<String, StatusStatistics> result = SortHelper.sortMap(m_status,
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
