package com.dianping.cat.service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.dal.jdbc.DalNotFoundException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.ServerConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;

public class ProjectService implements Initializable {

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private ServerConfigManager m_manager;

	private Map<String, Project> m_projects = new ConcurrentHashMap<String, Project>();

	private Set<String> m_domains = new HashSet<String>();

	public void addDomain(String domain) {
		m_domains.add(domain);
	}

	public boolean containsDomainInCat(String domain) {
		return m_domains.contains(domain);
	}

	public Project createLocal() {
		return m_projectDao.createLocal();
	}

	public boolean deleteProject(Project project) {
		int id = project.getId();
		Iterator<Entry<String, Project>> iterator = m_projects.entrySet().iterator();
		String domainName = null;

		while (iterator.hasNext()) {
			Entry<String, Project> entry = iterator.next();
			Project pro = entry.getValue();

			if (pro.getId() == id) {
				domainName = pro.getDomain();
				break;
			}
		}

		try {
			m_projects.remove(domainName);
			m_projectDao.deleteByPK(project);
			return true;
		} catch (Exception e) {
			Cat.logError("delete project error ", e);
			return false;
		}
	}

	public List<Project> findAll() throws DalException {
		return new ArrayList<Project>(m_projects.values());
	}

	public Set<String> findAllDomain() {
		return m_domains;
	}

	public Project findByDomain(String domainName) {
		Project project = m_projects.get(domainName);

		if (project != null) {
			return project;
		} else {
			try {
				Project pro = m_projectDao.findByDomain(domainName, ProjectEntity.READSET_FULL);

				m_projects.put(pro.getDomain(), pro);
				return project;
			} catch (DalException e) {
			} catch (Exception e) {
				Cat.logError(e);
			}
			return null;
		}
	}

	public Project findByCmdbDomain(String domainName) {
		try {
			Project project = m_projectDao.findByDomain(domainName, ProjectEntity.READSET_FULL);
			return project;
		} catch (DalException e) {
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	public Map<String, Project> findAllProjects() {
		return m_projects;
	}

	public Project findProject(int id) {
		Iterator<Entry<String, Project>> iterator = m_projects.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, Project> entry = iterator.next();
			Project pro = entry.getValue();

			if (pro.getId() == id) {
				return pro;
			}
		}

		try {
			Project project = m_projectDao.findByPK(id, ProjectEntity.READSET_FULL);

			m_projects.put(project.getDomain(), project);
			return project;
		} catch (DalNotFoundException e) {
		} catch (Exception e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	public void initialize() throws InitializationException {
		if (!m_manager.isLocalMode()) {
			Threads.forGroup("cat").start(new ProjectReloadTask());
		}
	}

	public boolean insert(Project project) throws DalException {
		m_projects.put(project.getDomain(), project);

		int result = m_projectDao.insert(project);
		if (result == 1) {
			return true;
		} else {
			return false;
		}
	}

	public boolean insertDomain(String domain) {
		Project project = createLocal();

		project.setDomain(domain);
		project.setProjectLine("Default");
		project.setDepartment("Default");
		try {
			insert(project);
			m_domains.add(domain);

			return true;
		} catch (Exception ex) {
			Cat.logError(ex);
		}
		return false;
	}

	public void refresh() {
		try {
			List<Project> projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);
			Map<String, Project> tmpProjects = new ConcurrentHashMap<String, Project>();
			Set<String> tmpDomains = new HashSet<String>();

			for (Project project : projects) {
				tmpDomains.add(project.getDomain());
				tmpProjects.put(project.getDomain(), project);
			}
			m_domains = tmpDomains;
			m_projects = tmpProjects;
		} catch (DalException e) {
			Cat.logError("initialize ProjectService error", e);
		}
	}

	public boolean updateProject(Project project) {
		m_projects.put(project.getDomain(), project);

		try {
			m_projectDao.updateByPK(project, ProjectEntity.UPDATESET_FULL);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	public class ProjectReloadTask implements Task {

		@Override
		public String getName() {
			return "project-reload";
		}

		@Override
		public void run() {
			boolean active = true;

			while (active) {
				try {
					refresh();
				} catch (Exception ex) {
					Cat.logError("reload project error", ex);
				}

				try {
					TimeUnit.MINUTES.sleep(1);
				} catch (InterruptedException ex) {
					active = false;
				}
			}
		}

		@Override
		public void shutdown() {
		}

	}

}
