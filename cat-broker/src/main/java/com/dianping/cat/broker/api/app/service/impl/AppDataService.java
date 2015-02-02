package com.dianping.cat.broker.api.app.service.impl;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppCommandDataDao;
import com.dianping.cat.broker.api.app.service.AppService;

public class AppDataService implements AppService<AppCommandData> {

	@Inject
	private AppCommandDataDao m_dao;

	public static final String ID = AppCommandData.class.getName();

	@Override
	public int[] insert(AppCommandData[] proto) throws DalException {
		return m_dao.insertOrUpdate(proto);
	}

	@Override
	public int insert(AppCommandData proto) throws DalException {
		return m_dao.insertOrUpdate(proto);
	}

}
