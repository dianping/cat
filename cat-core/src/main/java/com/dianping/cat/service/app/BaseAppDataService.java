package com.dianping.cat.service.app;

import org.unidal.dal.jdbc.DalException;

public interface BaseAppDataService<T> {

	public int[] insert(T[] proto) throws DalException;

	public void insertSingle(T proto) throws DalException;
}
