/**
 * 
 */
package com.dianping.cat.report.task;

import com.dianping.cat.hadoop.dal.Task;

/**
 * @author sean.wang
 * @since May 21, 2012
 */
public abstract class TaskConsumer implements Runnable {
	private static final int MAX_TODO_FAIL_TIMES = 3;

	public static final int STATUS_TODO = 1;

	public static final int STATUS_DOING = 2;

	public static final int STATUS_DONE = 3;

	public static final int STATUS_FAIL = 4;

	private volatile boolean running = true;

	private volatile boolean stopped = false;

	public void run() {
		findtask: while (running) {
			Task task = findDoingTask(); // find doing task
			if (task == null) {
				task = findTodoTask(); // find todo task
			}
			if (task != null) {
				if (task.getStatus() == TaskConsumer.STATUS_DOING || updateTodoToDoing(task)) { // confirm doing status
					int failTimes = 0;
					while (!processTask(task)) {
						failTimes++;
						if (failTimes < MAX_TODO_FAIL_TIMES) {
							taskFailDuration();
						} else {
							failTask(task);
							continue findtask;
						}
					}
					updateDoingToDone(task);
					mergeYesterdayReport();
				}
			} else {
				taskNotFindDuration();
			}
		}
		this.stopped = true;
	}

	protected abstract void failTask(Task todo);

	protected abstract void taskFailDuration();

	protected abstract void mergeYesterdayReport();

	protected abstract void taskNotFindDuration();

	protected abstract boolean updateTodoToDoing(Task todo);

	protected abstract Task findTodoTask();

	protected abstract void updateDoingToDone(Task doing);

	protected abstract boolean processTask(Task doing);

	protected abstract Task findDoingTask();

	public void stop() {
		this.running = false;
	}

	public boolean isStopped() {
		return this.stopped;
	}

}
