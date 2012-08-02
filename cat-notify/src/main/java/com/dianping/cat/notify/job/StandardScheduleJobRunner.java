package com.dianping.cat.notify.job;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.dianping.cat.notify.config.ConfigContext;
import com.dianping.cat.notify.server.AbstractContainerHolder;
import com.site.helper.Threads;

public class StandardScheduleJobRunner extends AbstractContainerHolder implements ScheduleJobRunner {
	private final static Logger logger = LoggerFactory.getLogger(StandardScheduleJobRunner.class);

	private List<ScheduleJob> m_jobs;

	private AtomicBoolean m_active = new AtomicBoolean();

	private ConfigContext m_config;

	private static final long SLEEP_TIME = 5 * 1000;// sleep for five seconds

	@Override
	public void run() {
		try {
			while (m_active.get()) {
				long timestamp = System.currentTimeMillis();// MilliSecondTimer.currentTimeMicros();
				for (ScheduleJob job : m_jobs) {
					if (job.isNeedToDo(timestamp)) {
						job.doJob(timestamp);
					}
				}
				Thread.sleep(SLEEP_TIME);
			}
		} catch (InterruptedException e) {
			// ignore it
		}
	}

	private boolean init() {
		if(m_jobs== null || m_jobs.size() ==0){
			logger.error("jobs is empty");
			return false;
		}
		JobContext jobContext = new JobContext();
		jobContext.addData("container", this);
		jobContext.addData("config", m_config);
		for(ScheduleJob job : m_jobs){
			job.init(jobContext);
		}
		m_active.set(true);
		return true;
	}

	@Override
	public void start() {
		if (init()) {
			Threads.forGroup("Cat-Notify-Job-Runner").start(this);
		} else {
			logger.error("fail to start the ScheduleJobRunner.");
		}
	}

	@Override
	public void stop() {
		m_active.set(false);
	}

	public void setConfig(ConfigContext config) {
		this.m_config = config;
	}

	public void setJobs(List<ScheduleJob> jobs) {
   	this.m_jobs = jobs;
   }

}
