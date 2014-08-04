package com.dianping.cat.service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;

public class ProjectService implements Initializable {

	@Inject
	private ProjectDao m_projectDao;

	private Map<String, Project> m_projects = new ConcurrentHashMap<String, Project>();

	public Project createLocal() {
		return m_projectDao.createLocal();
	}

	public boolean deleteProject(Project project) {
		int id = project.getId();
		Iterator<Entry<String, Project>> iterator = m_projects.entrySet().iterator();

		while (iterator.hasNext()) {
			Entry<String, Project> entry = iterator.next();
			Project pro = entry.getValue();
			if (pro.getId() == id) {
				iterator.remove();

				try {
					pro.setKeyId(pro.getId());
					m_projectDao.deleteByPK(pro);
					return true;
				} catch (Exception e) {
					Cat.logError("delete project error " + pro.toString(), e);
					return false;
				}
			}
		}

		return true;
	}

	public List<Project> findAll() throws DalException {
		return new ArrayList<Project>(m_projects.values());
	}

	public Project findByDomain(String domainName) {
		return m_projects.get(domainName);
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

		return new Project();
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			List<Project> projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);
			for (Project project : projects) {
				m_projects.put(project.getDomain(), project);
			}
		} catch (DalException e) {
			Cat.logError("initialize ProjectService error", e);
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
}
