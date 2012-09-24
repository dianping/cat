package com.dianping.dog.notify.job;

public interface ScheduleJob {

	boolean init(JobContext jobContext);

	boolean isNeedToDo(long timestamp);

	void doJob(long timestamp);

}
