package com.dianping.cat.broker.api.app.service.impl;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.app.AppConnectionData;
import com.dianping.cat.app.AppConnectionDataDao;
import com.dianping.cat.broker.api.app.service.AppService;

public class AppConnectionService implements AppService<AppConnectionData> {

	@Inject
	private AppConnectionDataDao m_dao;

	public static final String ID = AppConnectionData.class.getName();

	@Override
	public int[] insert(AppConnectionData[] proto) throws DalException {
		return m_dao.insertOrUpdate(proto);
	}

	@Override
	public int insert(AppConnectionData proto) throws DalException {
		return m_dao.insertOrUpdate(proto);
	}

}
