/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.Date;
import java.util.concurrent.locks.LockSupport;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.hadoop.dal.TaskDao;
import com.dianping.cat.hadoop.dal.TaskEntity;
import com.dianping.cat.report.task.spi.ReportFacade;
import com.site.dal.jdbc.DalException;
import com.site.lookup.annotation.Inject;

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
			// Cat.logError(e);
		}
		return task;
	}

	@Override
	protected Task findTodoTask() {
		Task task = null;
		try {
			task = m_taskDao.findByStatusConsumer(STATUS_TODO, null, TaskEntity.READSET_FULL);
		} catch (DalException e) {
			// Cat.logError(e);
		}
		return task;
	}

	@Override
	protected boolean processTask(Task doing) {
		return m_reportFacade.builderReport(doing);
	}

	@Override
	protected void taskNotFoundDuration() {
		Date awakeTime = TaskHelper.nextTaskTime();
		LockSupport.parkUntil(awakeTime.getTime());
	}

	@Override
	protected void taskRetryDuration() {
		LockSupport.parkNanos(10L * 1000 * 1000 * 1000);// sleep 10 sec
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
