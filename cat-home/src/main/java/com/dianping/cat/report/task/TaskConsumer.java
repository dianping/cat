/**
 * 
 */
package com.dianping.cat.report.task;

import java.util.concurrent.locks.LockSupport;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.hadoop.dal.Task;
import com.dianping.cat.message.Transaction;

public abstract class TaskConsumer implements Runnable {

	private static final int MAX_TODO_RETRY_TIMES = 3;

	public static final int STATUS_DOING = 2;

	public static final int STATUS_DONE = 3;

	public static final int STATUS_FAIL = 4;

	public static final int STATUS_TODO = 1;

	private long m_nanos = 2L * 1000 * 1000 * 1000;

	private volatile boolean running = true;

	private volatile boolean stopped = false;

	protected abstract Task findDoingTask(String consumerIp);

	protected abstract Task findTodoTask();

	protected String getLoaclIp() {
		return NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
	}

	protected long getSleepTime() {
		return m_nanos;
	}

	public boolean isStopped() {
		return this.stopped;
	}

	protected abstract boolean processTask(Task doing);

	public void run() {
		String localIp = getLoaclIp();
		while (running) {
			LockSupport.parkNanos(getSleepTime());
			Transaction t = Cat.newTransaction("System", "MergeJob-" + localIp);
			try {
				Task task = findDoingTask(localIp);
				if (task == null) {
					task = findTodoTask();
				}

				boolean again = false;
				if (task != null) {
					task.setConsumer(localIp);
					if (task.getStatus() == TaskConsumer.STATUS_DOING || updateTodoToDoing(task)) { // confirm
						int retryTimes = 0;
						while (!processTask(task)) {
							retryTimes++;
							if (retryTimes < MAX_TODO_RETRY_TIMES) {
								taskRetryDuration();
							} else {
								updateDoingToFailure(task);
								again = true;
								break;
							}
						}
						if (!again) {
							updateDoingToDone(task);
						}
					}
				} else {
					taskNotFoundDuration();
				}
				t.setStatus(Transaction.SUCCESS);
			} catch (Throwable e) {
				Cat.logError(e);
				t.setStatus(e);
			} finally {
				t.complete();
			}
		}
		this.stopped = true;
	}

	public void stop() {
		this.running = false;
	}

	protected abstract void taskNotFoundDuration();

	protected abstract void taskRetryDuration();

	protected abstract boolean updateDoingToDone(Task doing);

	protected abstract boolean updateDoingToFailure(Task todo);

	protected abstract boolean updateTodoToDoing(Task todo);
}
