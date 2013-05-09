package com.dianping.cat.system.alarm;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.dal.jdbc.DalException;
import org.unidal.helper.Threads;
import org.unidal.helper.Threads.Task;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.home.dal.report.ZabbixError;
import com.dianping.cat.home.dal.report.ZabbixErrorDao;

public class AlertManager implements Initializable, LogEnabled {

	@Inject
	private ZabbixErrorDao m_zabbixErrorDao;

	@Inject
	private ServerConfigManager m_manager;

	private BlockingQueue<ZabbixError> m_errors = new LinkedBlockingQueue<ZabbixError>(1000);

	private Logger m_logger;

	public boolean addError(ZabbixError error) {
		try {
			return m_errors.offer(error, 10, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Cat.logError(e);
		}
		return false;
	}

	@Override
	public void initialize() throws InitializationException {
		if (!m_manager.isLocalMode() && m_manager.isJobMachine()) {
			Threads.forGroup("Cat").start(new Job());
		}
	}

	public class Job implements Task {

		@Override
		public void run() {
			boolean active = true;
			while (active) {
				try {
					ZabbixError error = m_errors.poll(5, TimeUnit.MILLISECONDS);

					if (error != null) {
						m_zabbixErrorDao.insert(error);
					}
				} catch (InterruptedException e) {
					active = false;
				} catch (DalException e) {
					m_logger.error(e.getMessage(), e);
					Cat.logError(e);
				}
			}
		}

		@Override
		public String getName() {
			return null;
		}

		@Override
		public void shutdown() {
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

}
