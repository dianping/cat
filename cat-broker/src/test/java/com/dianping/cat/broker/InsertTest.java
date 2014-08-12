package com.dianping.cat.broker;

import java.util.Date;

import org.junit.Test;
import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.ComponentTestCase;

import com.dianping.cat.app.AppDataCommand;
import com.dianping.cat.app.AppDataCommandDao;

public class InsertTest extends ComponentTestCase {

	@Test
	public void testBatch() throws DalException {
		AppDataCommandDao dao = lookup(AppDataCommandDao.class);
		AppDataCommand[] commands = new AppDataCommand[100];

		for (int i = 0; i < 100; i++) {
			AppDataCommand command = new AppDataCommand();

			command.setCommandId(i % 3 + 1);
			command.setAccessNumber(i);
			command.setAccessNumberSum(i);
			command.setAppVersion(i);
			command.setCity(i);
			command.setCode(i);
			command.setConnnectType(i);
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
		AppDataCommandDao dao = lookup(AppDataCommandDao.class);
		AppDataCommand[] commands = new AppDataCommand[100];

		for (int i = 0; i < 100; i++) {
			AppDataCommand command = new AppDataCommand();

			command.setCommandId(i % 3 + 1);
			command.setAccessNumber(i);
			command.setAccessNumberSum(i);
			command.setAppVersion(i);
			command.setCity(i);
			command.setCode(i);
			command.setConnnectType(i);
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
