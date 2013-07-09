package com.dianping.cat.system.page.abtest.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.LockSupport;

import org.apache.commons.lang.StringUtils;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
import com.dianping.cat.abtest.model.entity.GroupstrategyDescriptor;
import com.dianping.cat.abtest.model.entity.Run;
import com.dianping.cat.core.dal.Project;
import com.dianping.cat.core.dal.ProjectDao;
import com.dianping.cat.core.dal.ProjectEntity;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestEntity;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.AbtestRunDao;
import com.dianping.cat.home.dal.abtest.AbtestRunEntity;
import com.dianping.cat.home.dal.abtest.GroupStrategy;
import com.dianping.cat.home.dal.abtest.GroupStrategyDao;
import com.dianping.cat.home.dal.abtest.GroupStrategyEntity;
import com.dianping.cat.system.page.abtest.AbtestStatus;
import com.dianping.cat.system.page.abtest.GroupStrategyParser;
import com.google.gson.Gson;

public class ABTestServiceImpl implements ABTestService, Initializable, Task {

	@Inject
	private AbtestDao m_abtestDao;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	@Inject
	private GroupStrategyDao m_groupStrategyDao;

	@Inject
	private GroupStrategyParser m_parser;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private int m_refreshTimeInSeconds = 60;

	private Map<Integer, Abtest> m_abtestMap = new ConcurrentHashMap<Integer, Abtest>();

	private Map<Integer, AbtestRun> m_abtestRunMap = new ConcurrentHashMap<Integer, AbtestRun>();

	private Map<Integer, GroupStrategy> m_groupStrategyMap = new ConcurrentHashMap<Integer, GroupStrategy>();

	@Override
	public Abtest getABTestByRunId(int id) {
		AbtestRun run = getAbtestRunById(id);
		Abtest ab = null;

		if (run != null) {
			ab = m_abtestMap.get(run.getCaseId());

			if (ab == null) {
				try {
					ab = m_abtestDao.findByPK(run.getCaseId(), AbtestEntity.READSET_FULL);

					m_abtestMap.put(run.getCaseId(), ab);
				} catch (Throwable e) {
					Cat.logError(e);
				}
			}
		}

		return ab;
	}

	@Override
	public AbtestModel getAbtestModelByStatus(AbtestStatus... status) {
		AbtestModel model = new AbtestModel();

		if (!m_abtestRunMap.isEmpty()) {
			Date now = new Date();

			for (AbtestRun run : m_abtestRunMap.values()) {

				Abtest entity = getABTestByRunId(run.getId());

				if (entity != null) {
					GroupStrategy groupStrategy = getGroupStrategyById(entity.getGroupStrategy());

					if (groupStrategy != null) {
						if (status.length == 0) {
							Case _case = transform(entity, run, groupStrategy);
							model.addCase(_case);
						} else {
							AbtestStatus _status = AbtestStatus.calculateStatus(run, now);

							for (AbtestStatus st : status) {
								if (st == _status) {
									Case _case = transform(entity, run, groupStrategy);
									model.addCase(_case);
								}
							}
						}
					}
				}
			}
		}
		return model;
	}

	@Override
	public AbtestRun getAbtestRunById(int id) {
		AbtestRun abtetRun = m_abtestRunMap.get(id);

		if (abtetRun == null) {
			try {
				abtetRun = m_abtestRunDao.findByPK(id, AbtestRunEntity.READSET_FULL);

				m_abtestRunMap.put(id, abtetRun);
			} catch (Throwable e) {
				Cat.logError(e);
			}
		}

		return abtetRun;
	}

	@Override
	public List<GroupStrategy> getAllGroupStrategies() {
		try {
			return m_groupStrategyDao.findAllByStatus(1, GroupStrategyEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}
		return null;
	}

	@Override
	public Map<String, List<Project>> getAllProjects() {
		List<Project> projects = new ArrayList<Project>();

		try {
			projects = m_projectDao.findAll(ProjectEntity.READSET_FULL);
		} catch (Exception e) {
			Cat.logError(e);
		}

		Map<String, List<Project>> result = new TreeMap<String, List<Project>>();
		if (projects != null) {
			for (Project project : projects) {
				String key = project.getDepartment() + "-" + project.getProjectLine();
				List<Project> list = result.get(key);
				if (list == null) {
					list = new ArrayList<Project>();
					result.put(key, list);
				}
				list.add(project);
			}
		}

		return result;
	}

	@Override
	public GroupStrategy getGroupStrategyById(int id) {
		GroupStrategy groupStrategy = m_groupStrategyMap.get(id);

		if (groupStrategy == null) {
			try {
				groupStrategy = m_groupStrategyDao.findByPK(id, GroupStrategyEntity.READSET_FULL);

				m_groupStrategyMap.put(id, groupStrategy);
			} catch (Throwable e) {
				Cat.logError(e);
			}
		}

		return groupStrategy;
	}

	@Override
	public String getName() {
		return getClass().getSimpleName();
	}

	@Override
	public void initialize() throws InitializationException {
		refresh();
	}

	@Override
	public void refresh() {
		try {
			Map<Integer, Abtest> abtestMap = new ConcurrentHashMap<Integer, Abtest>();
			Map<Integer, AbtestRun> abtestRunMap = new ConcurrentHashMap<Integer, AbtestRun>();
			Map<Integer, GroupStrategy> groupStrategyMap = new ConcurrentHashMap<Integer, GroupStrategy>();

			List<Abtest> abtests = m_abtestDao.findAll(AbtestEntity.READSET_FULL);

			for (Abtest abtest : abtests) {
				abtestMap.put(abtest.getId(), abtest);
			}

			List<AbtestRun> abtestRuns = m_abtestRunDao.findAll(AbtestRunEntity.READSET_FULL);

			for (AbtestRun abtestRun : abtestRuns) {
				abtestRunMap.put(abtestRun.getId(), abtestRun);
			}

			List<GroupStrategy> groupStrategies = m_groupStrategyDao.findAll(GroupStrategyEntity.READSET_FULL);

			for (GroupStrategy groupStrategy : groupStrategies) {
				groupStrategyMap.put(groupStrategy.getId(), groupStrategy);
			}

			// switch
			m_abtestMap = abtestMap;
			m_abtestRunMap = abtestRunMap;
			m_groupStrategyMap = groupStrategyMap;
		} catch (Throwable e) {
			Cat.logError(e);
		}
	}

	@Override
	public void run() {
		while (true) {
			long start = System.currentTimeMillis();

			try {
				refresh();
			} catch (Throwable e) {
				Cat.logError(e);
			}

			LockSupport.parkUntil(start + m_refreshTimeInSeconds * 1000L); // every minute
		}
	}

	public void setAbtestDao(AbtestDao abtestDao) {
		m_abtestDao = abtestDao;
	}

	public void setAbtestMap(Map<Integer, Abtest> abtestMap) {
		m_abtestMap = abtestMap;
	}

	public void setAbtestRunDao(AbtestRunDao abtestRunDao) {
		m_abtestRunDao = abtestRunDao;
	}

	public void setAbtestRunMap(Map<Integer, AbtestRun> abtestRunMap) {
		m_abtestRunMap = abtestRunMap;
	}

	public void setGroupStrategyDao(GroupStrategyDao groupStrategyDao) {
		m_groupStrategyDao = groupStrategyDao;
	}

	public void setGroupStrategyMap(Map<Integer, GroupStrategy> groupStrategyMap) {
		m_groupStrategyMap = groupStrategyMap;
	}

	public void setParser(GroupStrategyParser parser) {
		m_parser = parser;
	}

	public void setProjectDao(ProjectDao projectDao) {
		m_projectDao = projectDao;
	}

	public void setRefreshTimeInSeconds(int refreshTimeInSeconds) {
		m_refreshTimeInSeconds = refreshTimeInSeconds;
	}

	@Override
	public void shutdown() {
	}

	private Case transform(Abtest abtest, AbtestRun run, GroupStrategy groupStrategy) {
		Case abCase = new Case(abtest.getId());

		abCase.setCreatedDate(abtest.getCreationDate());
		abCase.setDescription(abtest.getDescription());
		abCase.setGroupStrategy(groupStrategy.getName());
		abCase.setName(abtest.getName());
		abCase.setOwner(abtest.getOwner());
		abCase.setLastModifiedDate(abtest.getModifiedDate());
		for (String domain : StringUtils.split(abtest.getDomains(), ',')) {
			abCase.addDomain(domain);
		}

		Run abRun = new Run(run.getId());
		Gson gson = m_parser.getGsonBuilder().create();

		for (String domain : StringUtils.split(run.getDomains(), ',')) {
			abRun.addDomain(domain);
		}
		abRun.setCreator(run.getCreator());
		abRun.setDisabled(false);
		abRun.setEndDate(run.getEndDate());
		abRun.setGroupstrategyDescriptor(gson.fromJson(run.getStrategyConfiguration(), GroupstrategyDescriptor.class));
		abRun.setStartDate(run.getStartDate());

		abCase.addRun(abRun);
		return abCase;
	}
}
