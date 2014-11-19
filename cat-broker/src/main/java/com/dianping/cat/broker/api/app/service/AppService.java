package com.dianping.cat.broker.api.app.service;

import org.unidal.dal.jdbc.DalException;

public interface AppService<T> {

	public int[] insert(T[] proto) throws DalException;

	public void insertSingle(T proto) throws DalException;
}
