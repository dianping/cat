/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dianping.cat.service;

import com.dianping.cat.Cat;
import com.dianping.cat.config.server.ServerConfigManager;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

@Named
public class ProjectService implements Initializable {

	public static final String DEFAULT = "Default";

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private ServerConfigManager m_manager;

	private ConcurrentHashMap<String, String> m_domains = new ConcurrentHashMap<String, String>();

	private ConcurrentHashMap<String, Project> m_domainToProjects = new ConcurrentHashMap<String, Project>();

	private ConcurrentHashMap<String, Project> m_cmdbToProjects = new ConcurrentHashMap<String, Project>();

	public boolean contains(String domain) {
		return m_domains.containsKey(domain);
	}

	public Project create() {
		return m_projectDao.createLocal();
	}

	public boolean delete(Project project) {
		int id = project.getId();
		String domainName = null;

		for (Entry<String, Project> entry : m_domainToProjects.entrySet()) {
			Project pro = entry.getValue();

			if (pro.getId() == id) {
				domainName = pro.getDomain();
				break;
			}
		}

		try {
			m_projectDao.deleteByPK(project);

			if (domainName != null) {
				m_domainToProjects.remove(domainName);
				m_domains.remove(domainName);
			}

			String cmdbDomain = project.getCmdbDomain();

			if (cmdbDomain != null) {
				m_cmdbToProjects.remove(cmdbDomain);
			}

			return true;
		} catch (Exception e) {
			Cat.logError("delete project error ", e);
			return false;
		}
	}

	public List<Project> findAll() throws DalException {
		return new ArrayList<Project>(m_domainToProjects.values());
	}

	public Set<String> findAllDomains() {
		return m_domains.keySet();
	}

	public Project findByDomain(String domainName) {
		Project project = m_domainToProjects.get(domainName);

		if (project != null) {
			return project;
		} else {
			try {
				Project pro = m_projectDao.findByDomain(domainName, ProjectEntity.READSET_FULL);

				m_domainToProjects.put(pro.getDomain(), pro);
				return project;
			} catch (DalException e) {
			} catch (Exception e) {
				Cat.logError(e);
			}
			return null;
		}
	}

	public Map<String, Department> findDepartments(Collection<String> domains) {
		Map<String, Department> departments = new TreeMap<String, Department>();

		for (String domain : domains) {
			Project project = findProject(domain);
			String department = DEFAULT;
			String projectLine = DEFAULT;

			if (project != null) {
				String bu = project.getBu();
				String productLine = project.getCmdbProductline();

				department = bu == null ? DEFAULT : bu;
				projectLine = productLine == null ? DEFAULT : productLine;
			}
			Department temp = departments.get(department);

			if (temp == null) {
				temp = new Department();
				departments.put(department, temp);
			}
			temp.findOrCreatProjectLine(projectLine).addDomain(domain);
		}

		return departments;
	}

	public Project findProject(String domain) {
		Project project = m_domainToProjects.get(domain);

		if (project == null) {
			project = m_cmdbToProjects.get(domain);
		}
		return project;
	}

	@Override
	public void initialize() throws InitializationException {
		if (!m_manager.isLocalMode()) {
			refresh();
		}
	}

	public boolean insert(Project project) {
		m_domainToProjects.put(project.getDomain(), project);

		try {
			int result = m_projectDao.insert(project);

			if (result == 1) {
				return true;
			} else {
				return false;
			}
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	public boolean insert(String domain) {
		Project project = create();

		project.setDomain(domain);
		project.setCmdbProductline(DEFAULT);
		project.setBu(DEFAULT);

		try {
			insert(project);
			m_domains.put(domain, domain);

			return true;
		} catch (Exception ex) {
			Cat.logError(ex);
		}
		return false;
	}

	protected void refresh() {
		try {
			List<Project> projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);
			ConcurrentHashMap<String, Project> tmpDomainProjects = new ConcurrentHashMap<String, Project>();
			ConcurrentHashMap<String, Project> tmpCmdbProjects = new ConcurrentHashMap<String, Project>();
			ConcurrentHashMap<String, String> tmpDomains = new ConcurrentHashMap<String, String>();

			for (Project project : projects) {
				String domain = project.getDomain();

				tmpDomains.put(domain, domain);
				tmpDomainProjects.put(domain, project);

				String cmdb = project.getCmdbDomain();

				if (cmdb != null) {
					tmpCmdbProjects.put(cmdb, project);
				}
			}
			m_domains = tmpDomains;
			m_domainToProjects = tmpDomainProjects;
			m_cmdbToProjects = tmpCmdbProjects;
		} catch (DalException e) {
			Cat.logError("initialize ProjectService error", e);
		}
	}

	public boolean update(Project project) {
		m_domainToProjects.put(project.getDomain(), project);

		try {
			m_projectDao.updateByPK(project, ProjectEntity.UPDATESET_FULL);
			return true;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	public static class Department {

		private Map<String, ProjectLine> m_projectLines = new TreeMap<String, ProjectLine>();

		public ProjectLine findOrCreatProjectLine(String projectLine) {
			ProjectLine line = m_projectLines.get(String.valueOf(projectLine));

			if (line == null) {
				line = new ProjectLine();

				m_projectLines.put(projectLine, line);
			}
			return line;
		}

		public Map<String, ProjectLine> getProjectLines() {
			return m_projectLines;
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
	}

}
