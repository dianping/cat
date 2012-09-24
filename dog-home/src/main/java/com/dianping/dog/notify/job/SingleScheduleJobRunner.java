package com.dianping.dog.notify.job;

import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.dog.notify.config.ConfigContext;
import com.dianping.dog.notify.report.DefaultContainerHolder;
import com.site.helper.Threads;
import com.site.lookup.annotation.Inject;

public class SingleScheduleJobRunner implements ScheduleJobRunner, LogEnabled {

	@Inject
	private ScheduleJob m_scheduleJob;

	@Inject
	private ConfigContext m_configContext;

	@Inject
	private DefaultContainerHolder m_defaultContainerHolder;

	private AtomicBoolean m_active = new AtomicBoolean();

	private Logger m_logger;

	private static final long SLEEP_TIME = 5 * 1000;

	@Override
	public void run() {
		try {
			while (m_active.get()) {
				long timestamp = System.currentTimeMillis();
				if (m_scheduleJob.isNeedToDo(timestamp)) {
					m_scheduleJob.doJob(timestamp);
				}
				Thread.sleep(SLEEP_TIME);
			}
		} catch (InterruptedException e) {
			m_logger.error(" thread is exit ", e);
		}
	}

	private boolean init() {
		if (m_scheduleJob == null) {
			m_logger.error("jobs is empty");
			return false;
		}
		JobContext jobContext = new JobContext();
		jobContext.addData("container", m_defaultContainerHolder);
		jobContext.addData("config", m_configContext);
		m_scheduleJob.init(jobContext);
		m_active.set(true);
		return true;
	}

	@Override
	public void start() {
		if (init()) {
			Threads.forGroup("Cat-Notify-Job-Runner").start(this);
		} else {
			m_logger.error("fail to start the ScheduleJobRunner.");
		}
	}

	@Override
	public void stop() {
		m_active.set(false);
	}

	public void setConfig(ConfigContext config) {
		this.m_configContext = config;
	}

	@Override
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}

}
