/**
 * 
 */
package com.dianping.cat.report.task;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Task;

/**
 * @author sean.wang
 * @since May 21, 2012
 */
public abstract class TaskConsumer implements Runnable, LogEnabled {
	private static final int MAX_TODO_RETRY_TIMES = 3;

	public static final int STATUS_TODO = 1;

	public static final int STATUS_DOING = 2;

	public static final int STATUS_DONE = 3;

	public static final int STATUS_FAIL = 4;

	private volatile boolean running = true;

	private volatile boolean stopped = false;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public void run() {
		m_logger.info("TaskConsumer start running.");

		findtask: while (running) {
			String localIp = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			Task task = findDoingTask(localIp); // find doing task
			if (task == null) {
				task = findTodoTask(); // find todo task
			}
			if (task != null) {
				task.setConsumer(localIp);
				if (task.getStatus() == TaskConsumer.STATUS_DOING || updateTodoToDoing(task)) { // confirm doing status
					int retryTimes = 0;
					while (!processTask(task)) {
						retryTimes++;
						if (retryTimes < MAX_TODO_RETRY_TIMES) {
							m_logger.warn("TaskConsumer retry " + retryTimes + ", " + task.toString());
							taskRetryDuration();
						} else {
							m_logger.error("TaskConsumer failed, " + task.toString());
							updateDoingToFailure(task);
							continue findtask;
						}
					}
					updateDoingToDone(task);
					mergeReport();
				}
			} else {
				taskNotFoundDuration();
			}
		}
		this.stopped = true;

		m_logger.info("TaskConsumer stoped.");
	}

	protected abstract boolean updateDoingToFailure(Task todo);

	protected abstract void taskRetryDuration();

	protected abstract void mergeReport();

	protected abstract void taskNotFoundDuration();

	protected abstract boolean updateTodoToDoing(Task todo);

	protected abstract Task findTodoTask();

	protected abstract boolean updateDoingToDone(Task doing);

	protected abstract boolean processTask(Task doing);

	protected abstract Task findDoingTask(String consumerIp);

	public void stop() {
		this.running = false;
	}

	public boolean isStopped() {
		return this.stopped;
	}

}
