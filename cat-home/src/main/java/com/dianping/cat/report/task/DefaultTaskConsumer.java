/*
 * Copyright (c) 2011-2018, Meituan Dianping. All Rights Reserved.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
/**
	*
	*/
package com.dianping.cat.report.task;

import java.util.Date;
import java.util.concurrent.locks.LockSupport;

import org.unidal.dal.jdbc.DalException;
import org.unidal.lookup.annotation.Inject;
import org.unidal.lookup.annotation.Named;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.Task;
import com.dianping.cat.core.dal.TaskDao;
import com.dianping.cat.core.dal.TaskEntity;
import com.dianping.cat.message.Transaction;

@Named
public class DefaultTaskConsumer extends TaskConsumer {

	@Inject
	private ReportFacade m_reportFacade;

	@Inject
	private TaskDao m_taskDao;

	@Override
	protected Task findDoingTask(String ip) {
		Task task = null;
		try {
			task = m_taskDao.findByStatusConsumer(STATUS_DOING, ip, TaskEntity.READSET_FULL);
		} catch (DalException e) {
		}
		return task;
	}

	@Override
	protected Task findTodoTask() {
		Task task = null;
		try {
			task = m_taskDao.findByStatusConsumer(STATUS_TODO, null, TaskEntity.READSET_FULL);
		} catch (DalException e) {
		}
		return task;
	}

	@Override
	public String getName() {
		return "Task-Consumer";
	}

	@Override
	protected boolean processTask(Task doing) {
		boolean result = false;
		Transaction t = Cat.newTransaction("Task", doing.getReportName());

		t.addData(doing.toString());
		try {
			result = m_reportFacade.builderReport(doing);
			t.setStatus(Transaction.SUCCESS);
		} catch (Throwable e) {
			Cat.logError(e);
			t.setStatus(e);
		} finally {
			t.complete();
		}
		return result;
	}

	@Override
	public void shutdown() {

	}

	@Override
	protected void taskNotFoundDuration() {
		try {
			Thread.sleep(2 * 60 * 1000);
		} catch (InterruptedException e) {
			// Ignore
		}
	}

	@Override
	protected void taskRetryDuration() {
		LockSupport.parkNanos(2L * 1000 * 1000 * 1000);// sleep 10 sec
	}

	@Override
	protected boolean updateDoingToDone(Task doing) {
		doing.setStatus(STATUS_DONE);
		doing.setEndDate(new Date());

		try {
			return m_taskDao.updateDoingToDone(doing, TaskEntity.UPDATESET_FULL) == 1;
		} catch (DalException e) {
			Cat.logError(e);
		}
		return true;
	}

	@Override
	protected boolean updateDoingToFailure(Task doing) {
		doing.setStatus(STATUS_FAIL);
		doing.setEndDate(new Date());

		try {
			return m_taskDao.updateDoingToFail(doing, TaskEntity.UPDATESET_FULL) == 1;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}

	@Override
	protected boolean updateTodoToDoing(Task todo) {
		todo.setStatus(STATUS_DOING);
		todo.setConsumer(NetworkInterfaceManager.INSTANCE.getLocalHostAddress());
		todo.setStartDate(new Date());

		try {
			return m_taskDao.updateTodoToDoing(todo, TaskEntity.UPDATESET_FULL) == 1;
		} catch (DalException e) {
			Cat.logError(e);
			return false;
		}
	}
}
