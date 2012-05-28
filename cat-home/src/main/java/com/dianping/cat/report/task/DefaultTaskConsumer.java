/**
 * 
 */
package com.dianping.cat.report.task;

import com.dianping.cat.hadoop.dal.Task;

/**
 * @author sean.wang
 * @since May 28, 2012
 */
public class DefaultTaskConsumer extends TaskConsumer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#failTodoTask(com.dianping.cat.hadoop.dal.Task)
	 */
	@Override
	protected void failTask(Task todo) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#todoTaskFailSleep()
	 */
	@Override
	protected void taskFailDuration() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#mergeYesterdayReport()
	 */
	@Override
	protected void mergeYesterdayReport() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#taskNotFindSleep()
	 */
	@Override
	protected void taskNotFindDuration() {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#updateTodoStatus(com.dianping.cat.hadoop.dal.Task)
	 */
	@Override
	protected boolean updateTodoToDoing(Task todo) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#findTodoTask()
	 */
	@Override
	protected Task findTodoTask() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#changeDoingStatus(com.dianping.cat.hadoop.dal.Task)
	 */
	@Override
	protected void updateDoingToDone(Task doing) {
		// TODO Auto-generated method stub

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#doTodoTask(com.dianping.cat.hadoop.dal.Task)
	 */
	@Override
	protected boolean processTask(Task doing) {
		// TODO Auto-generated method stub
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.report.task.TaskConsumer#findDoingTask()
	 */
	@Override
	protected Task findDoingTask() {
		// TODO Auto-generated method stub
		return null;
	}

}
