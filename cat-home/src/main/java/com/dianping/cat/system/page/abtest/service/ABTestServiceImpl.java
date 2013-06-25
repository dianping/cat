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

	private Map<Integer, String> m_abtestMap = new ConcurrentHashMap<Integer, String>();

	@Override
	public String getABTestNameByRunId(int id) {
		String name = m_abtestMap.get(id);

		if (name != null) {
			return name;
		} else {
			try {
				AbtestRun run = m_abtestRunDao.findByPK(id, AbtestRunEntity.READSET_FULL);
				Abtest abtest = m_abtestDao.findByPK(run.getCaseId(), AbtestEntity.READSET_FULL);

				name = abtest.getName();
				m_abtestMap.put(id, name);
			} catch (DalException e) {
				Cat.logError(e);
			}
		}

		return name;
	}
}
