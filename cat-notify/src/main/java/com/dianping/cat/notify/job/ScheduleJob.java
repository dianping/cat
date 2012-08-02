package com.dianping.cat.notify.job;

public interface ScheduleJob {

	boolean init(JobContext jobContext);

	boolean isNeedToDo(long timestamp);

	void doJob(long timestamp);

}
