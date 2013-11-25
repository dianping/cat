package com.dianping.cat.system.page.abtest.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.LockSupport;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.abtest.model.entity.AbtestModel;
import com.dianping.cat.abtest.model.entity.Case;
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
import com.dianping.cat.system.page.abtest.util.AbtestStatus;
import com.dianping.cat.system.page.abtest.util.AbtestStatus.AbtestStatusUtil;
import com.dianping.cat.system.page.abtest.util.CaseBuilder;

public class ABTestServiceImpl implements ABTestService, Initializable, Task {

	@Inject
	private AbtestDao m_abtestDao;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	@Inject
	private GroupStrategyDao m_groupStrategyDao;

	@Inject
	private CaseBuilder m_caseBuilder;

	@Inject
	private ProjectDao m_projectDao;

	@Inject
	private int m_refreshTimeInSeconds = 60;

	private Map<Integer, Abtest> m_abtestMap = new ConcurrentHashMap<Integer, Abtest>();

	private Map<Integer, AbtestRun> m_abtestRunMap = new ConcurrentHashMap<Integer, AbtestRun>();

	private Map<Integer, GroupStrategy> m_groupStrategyMap = new ConcurrentHashMap<Integer, GroupStrategy>();

	private long m_lastRefreshTime = -1;

	private long m_modifyTime = 0;

	private AbtestStatusUtil statusUtil = new AbtestStatusUtil();

	@Override
	public Abtest getABTestByRunId(int runId) {
		AbtestRun run = getAbTestRunById(runId);
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

	public AbtestModel getABTestModelByRunID(int runId) {
		AbtestModel model = new AbtestModel();

		try {
			Abtest abtest = getABTestByRunId(runId);
			AbtestRun run = getAbTestRunById(runId);

			GroupStrategy groupStrategy = getGroupStrategyById(abtest.getGroupStrategy());

			Case abtestCase = m_caseBuilder.build(abtest, run, groupStrategy);

			model.addCase(abtestCase);
		} catch (Throwable e) {
			Cat.logError(e);
		}

		return model;
	}

	@Override
	public AbtestModel getABTestModelByStatus(AbtestStatus... status) {
		AbtestModel model = new AbtestModel();

		if (!m_abtestRunMap.isEmpty()) {
			Date now = new Date();

			for (AbtestRun run : m_abtestRunMap.values()) {
				try {
					Abtest abtest = getABTestByRunId(run.getId());

					GroupStrategy groupStrategy = getGroupStrategyById(abtest.getGroupStrategy());

					Case abtestCase = m_caseBuilder.build(abtest, run, groupStrategy);

					if (status.length == 0) {
						model.addCase(abtestCase);
					} else {
						AbtestStatus abtestStatus = statusUtil.calculateStatus(run, now);

						for (AbtestStatus st : status) {
							if (st == abtestStatus) {
								model.addCase(abtestCase);
							}
						}
					}
				} catch (Throwable e) {
					Cat.logError(e);
				}
			}
		}

		return model;
	}

	@Override
	public AbtestRun getAbTestRunById(int id) {
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
	public List<AbtestRun> getAbtestRunByStatus(AbtestStatus status) {
		List<AbtestRun> runs = new ArrayList<AbtestRun>();
		Date now = new Date();

		for (AbtestRun run : m_abtestRunMap.values()) {
			AbtestStatus _status = statusUtil.calculateStatus(run, now);

			if (_status == status) {
				runs.add(run);
			}
		}

		return runs;
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
				groupStrategy = new GroupStrategy();
			}
		}

		return groupStrategy;
	}

	@Override
	public long getModifiedTime() {
		return m_modifyTime;
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
		if (m_modifyTime > m_lastRefreshTime) {
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

				m_lastRefreshTime = m_modifyTime;
			} catch (Throwable e) {
				Cat.logError(e);
			}
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

	@Override
	public synchronized void setModified() {
		m_modifyTime = System.currentTimeMillis();
	}

	public void setRefreshTimeInSeconds(int refreshTimeInSeconds) {
		m_refreshTimeInSeconds = refreshTimeInSeconds;
	}

	@Override
	public void shutdown() {
	}
}
