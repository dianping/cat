package com.dianping.cat.system.page.abtest.service;

import java.util.Map;

import org.jboss.netty.util.internal.ConcurrentHashMap;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.home.dal.abtest.Abtest;
import com.dianping.cat.home.dal.abtest.AbtestDao;
import com.dianping.cat.home.dal.abtest.AbtestEntity;
import com.dianping.cat.home.dal.abtest.AbtestRun;
import com.dianping.cat.home.dal.abtest.AbtestRunDao;
import com.dianping.cat.home.dal.abtest.AbtestRunEntity;

public class ABTestServiceImpl implements ABTestService {

	@Inject
	private AbtestDao m_abtestDao;

	@Inject
	private AbtestRunDao m_abtestRunDao;

	private Map<Integer, Abtest> m_abtestMap = new ConcurrentHashMap<Integer, Abtest>();

	@Override
	public Abtest getABTestNameByRunId(int id) {
		Abtest name = m_abtestMap.get(id);

		if (name != null) {
			return name;
		} else {
			try {
				AbtestRun run = m_abtestRunDao.findByPK(id, AbtestRunEntity.READSET_FULL);
				Abtest abtest = m_abtestDao.findByPK(run.getCaseId(), AbtestEntity.READSET_FULL);

				m_abtestMap.put(id, abtest);
			} catch (DalException e) {
				Cat.logError(e);
			}
		}

		return null;
	}
}
