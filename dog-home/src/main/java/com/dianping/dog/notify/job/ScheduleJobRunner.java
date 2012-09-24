package com.dianping.dog.notify.job;

public interface ScheduleJobRunner extends Runnable {

	void start();

	void stop();

}
