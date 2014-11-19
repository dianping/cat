package com.dianping.cat.broker.api.app.service.impl;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.app.AppDataCommandDao;
import com.dianping.cat.broker.api.app.service.AppService;

public class AppDataService implements AppService<AppDataCommand> {

	@Inject
	private AppDataCommandDao m_dao;

	public static final String ID = AppDataCommand.class.getName();

	@Override
	public int[] insert(AppDataCommand[] proto) throws DalException {
		return m_dao.insert(proto);
	}

	@Override
	public void insertSingle(AppDataCommand proto) throws DalException {
		m_dao.insert(proto);
	}

}
