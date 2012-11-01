package com.dianping.cat.report.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dainping.cat.consumer.dal.report.Project;
import com.dainping.cat.consumer.dal.report.ProjectDao;
import com.dainping.cat.consumer.dal.report.ProjectEntity;
import com.dianping.cat.Cat;
import com.dianping.cat.helper.TimeUtil;
import com.site.dal.jdbc.DalException;
import com.site.helper.Threads;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class DomainNavManager implements Initializable {

	@Inject
	private ProjectDao m_projectDao;

	private static Map<String, Project> m_projects = new HashMap<String, Project>();

	public static Map<String, Department> getDepartment(Collection<String> domains) {
		Map<String, Department> result = new TreeMap<String, Department>();

		synchronized (m_projects) {
			for (String domain : domains) {
				Project project = m_projects.get(domain);
				String department = "Default";
				String projectLine = "Default";

				if (project != null) {
					department = project.getDepartment();
					projectLine = project.getProjectLine();
				}
				Department temp1 = result.get(department);
				if (temp1 == null) {
					temp1 = new Department();
					result.put(department, temp1);
				}
				temp1.findOrCreatProjectLine(projectLine).addDomain(domain);
			}
		}

		return result;
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			DomainReload reload = new DomainReload();

			Threads.forGroup("Cat").start(reload);
		} catch (Exception e) {
			Cat.logError(e);
		}
	}

	public void reloadDomainInfo() {
		synchronized (m_projects) {
			try {
				List<Project> projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);

				if (projects.size() > 0) {
					m_projects.clear();

					for (Project project : projects) {
						m_projects.put(project.getDomain(), project);
					}
				}
			} catch (DalException e) {
				Cat.logError(e);
			}
		}
	}

	public static class Department {

		private Map<String, ProjectLine> m_projectLines = new HashMap<String, ProjectLine>();

		public ProjectLine findOrCreatProjectLine(String projectLine) {
			if (projectLine == null) {
				projectLine = "Default";
			}

			ProjectLine line = m_projectLines.get(projectLine);

			if (line == null) {
				line = new ProjectLine();

				m_projectLines.put(projectLine, line);
			}
			return line;
		}

		public Map<String, ProjectLine> getProjectLines() {
			return m_projectLines;
		}

		public void setProjectLines(Map<String, ProjectLine> projectLines) {
			m_projectLines = projectLines;
		}
	}

	public class DomainReload implements Task {

		@Override
		public String getName() {
			return "Domain-Info-Reload";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					reloadDomainInfo();
				} catch (Exception e) {
					Cat.logError(e);
				}
				try {
					Thread.sleep(3 * TimeUtil.ONE_MINUTE);
				} catch (InterruptedException e) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}
	}

	public static class ProjectLine {
		private List<String> m_lineDomains = new ArrayList<String>();

		public void addDomain(String name) {
			m_lineDomains.add(name);
		}

		public List<String> getLineDomains() {
			return m_lineDomains;
		}

		public void setLineDomains(List<String> lineDomains) {
			m_lineDomains = lineDomains;
		}
	}
}
