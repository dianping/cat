/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.concurrent.locks.LockSupport;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Task;

/**
 * @author sean.wang
 * @since May 21, 2012
 */
public abstract class TaskConsumer implements Runnable {
	private static final int MAX_TODO_RETRY_TIMES = 3;

	public static final int STATUS_TODO = 1;

	public static final int STATUS_DOING = 2;

	public static final int STATUS_DONE = 3;

	public static final int STATUS_FAIL = 4;

	private volatile boolean running = true;

	private volatile boolean stopped = false;

	public void run() {
		String localIp = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
		findtask: while (running) {
			LockSupport.parkNanos(2L * 1000 * 1000 * 1000);
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
							taskRetryDuration(task, retryTimes);
						} else {
							updateDoingToFailure(task);
							continue findtask;
						}
					}
					if (updateDoingToDone(task)) {
						mergeReport(task);
					}
				}
			} else {
				taskNotFoundDuration();
			}
		}
		this.stopped = true;
	}

	protected abstract boolean updateDoingToFailure(Task todo);

	protected abstract void taskRetryDuration(Task task, int retryTimes);

	protected abstract void mergeReport(Task task);

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
