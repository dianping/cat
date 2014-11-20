package com.dianping.cat.broker.api.app.service.impl;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.app.AppSpeedData;
import com.dianping.cat.app.AppSpeedDataDao;
import com.dianping.cat.broker.api.app.service.AppService;

public class AppSpeedService implements AppService<AppSpeedData> {

	@Inject
	private AppSpeedDataDao m_dao;

	public static final String ID = AppSpeedData.class.getName();

	@Override
	public int[] insert(AppSpeedData[] proto) throws DalException {
		return m_dao.insertOrUpdate(proto);
	}

	@Override
	public int insert(AppSpeedData proto) throws DalException {
		return m_dao.insertOrUpdate(proto);
	}

}