package com.dianping.cat.broker.app;

import java.util.Date;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.app.AppCommandData;
import com.dianping.cat.app.AppCommandDataDao;

public class InsertTest extends ComponentTestCase {

	@Test
	public void testBatch() throws DalException {
		AppCommandDataDao dao = lookup(AppCommandDataDao.class);
		AppCommandData[] commands = new AppCommandData[100];

		for (int i = 0; i < 100; i++) {
			AppCommandData command = new AppCommandData();

			command.setCommandId(i % 3 + 1);
			command.setAccessNumber(i);
			command.setAccessNumberSum(i);
			command.setAppVersion(i);
			command.setCity(i);
			command.setCode(i);
			command.setConnectType(i);
			command.setCreationDate(new Date());
			command.setPeriod(new Date());
			command.setPlatform(i);
			command.setRequestPackage(i);
			command.setResponsePackage(i);
			command.setResponseSumTime(i);
			command.setResponseSumTimeSum(i);
			command.setStatus(i);

		}
		dao.insert(commands);
	}

	@Test
	public void test() throws DalException {
		AppCommandDataDao dao = lookup(AppCommandDataDao.class);
		AppCommandData[] commands = new AppCommandData[100];

		for (int i = 0; i < 100; i++) {
			AppCommandData command = new AppCommandData();

			command.setCommandId(i % 3 + 1);
			command.setAccessNumber(i);
			command.setAccessNumberSum(i);
			command.setAppVersion(i);
			command.setCity(i);
			command.setCode(i);
			command.setConnectType(i);
			command.setCreationDate(new Date());
			command.setPeriod(new Date());
			command.setPlatform(i);
			command.setRequestPackage(i);
			command.setResponsePackage(i);
			command.setResponseSumTime(i);
			command.setResponseSumTimeSum(i);
			command.setStatus(i);

			commands[i] = command;

			dao.insert(command);
		}
	}
}
