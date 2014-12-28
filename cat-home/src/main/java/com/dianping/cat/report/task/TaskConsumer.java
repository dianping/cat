package com.dianping.cat.report.task;

import java.util.Calendar;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.core.dal.Task;

public abstract class TaskConsumer implements org.unidal.helper.Threads.Task {

	private static final int MAX_TODO_RETRY_TIMES = 1;

	public static final int STATUS_TODO = 1;

	public static final int STATUS_DOING = 2;

	public static final int STATUS_DONE = 3;

	public static final int STATUS_FAIL = 4;

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

	public boolean checkTime() {
		Calendar cal = Calendar.getInstance();
		int minute = cal.get(Calendar.MINUTE);

		if (minute > 15) {
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void run() {
		String localIp = getLoaclIp();
		while (running) {
			try {
				if (checkTime()) {
					Task task = findDoingTask(localIp);
					if (task == null) {
						task = findTodoTask();
					}
					boolean again = false;
					if (task != null) {
						try {
							task.setConsumer(localIp);
							if (task.getStatus() == TaskConsumer.STATUS_DOING || updateTodoToDoing(task)) {
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
						} catch (Throwable e) {
							Cat.logError(task.toString(), e);
						}
					} else {
						taskNotFoundDuration();
					}
				} else {
					try {
						Thread.sleep(60 * 1000);
					} catch (InterruptedException e) {
						// Ignore
					}
				}
			} catch (Throwable e) {
				Cat.logError(e);
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
