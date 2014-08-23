package com.dianping.cat.report.view;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.service.ProjectService;

public class DomainNavManager implements Task, Initializable {

	@Inject
	private ProjectService m_projectService;

	// key is domain
	private Map<String, Project> m_projects = new ConcurrentHashMap<String, Project>();

	public static final String DEFAULT = "Default";

	public Map<String, Department> getDepartment(Collection<String> domains) {
		Map<String, Department> result = new TreeMap<String, Department>();

		for (String domain : domains) {
			Project project = m_projects.get(domain);
			String department = DEFAULT;
			String projectLine = DEFAULT;

			if (project != null) {
				department = project.getBu() == null ? DEFAULT : project.getBu();
				projectLine = project.getCmdbProductline() == null ? DEFAULT : project.getCmdbProductline();

			}
			Department temp = result.get(department);

			if (temp == null) {
				temp = new Department();
				result.put(department, temp);
			}
			temp.findOrCreatProjectLine(projectLine).addDomain(domain);
		}

		return result;
	}

	public Collection<String> getDomains() {
		return m_projects.keySet();
	}

	@Override
	public String getName() {
		return "Domain-Info-Reload";
	}

	public Project getProjectByName(String domain) {
		return m_projects.get(domain);
	}

	public Map<String, Project> getProjects() {
		return m_projects;
	}

	@Override
	public void initialize() throws InitializationException {
		reloadDomainInfo();
	}

	public void reloadDomainInfo() {
		try {
			List<Project> projects = m_projectService.findAll();

			synchronized (m_projects) {
				if (projects.size() > 0) {
					for (Project project : projects) {
						m_projects.put(project.getDomain(), project);
					}
				}
			}
		} catch (DalException e) {
			Cat.logError(e);
		}
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

	public static class Department {

		private Map<String, ProjectLine> m_projectLines = new TreeMap<String, ProjectLine>();

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
