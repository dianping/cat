package com.dianping.cat.report.page.externalError;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import com.dianping.cat.helper.TimeUtil;
import com.dianping.cat.home.dal.report.Event;
import com.dianping.cat.home.dal.report.EventDao;
import com.dianping.cat.home.dal.report.EventEntity;

public class EventCollectManager implements Initializable, LogEnabled {

	public static int ZABBIX_ERROR = 1;

	public static int DB_ERROR = 2;

	public static int CAT_ERROR = 3;

	@Inject
	private EventDao m_eventDao;

	@Inject
	private ServerConfigManager m_manager;

	private BlockingQueue<Event> m_errors = new LinkedBlockingQueue<Event>(1000);

	private Logger m_logger;

	public List<Event> queryEvents(String domain, Date date) {
		Date start = new Date(date.getTime() - TimeUtil.ONE_MINUTE * 3);
		Date end = date;

		try {
			return m_eventDao.findByDomainTime(domain, start, end, EventEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}
		return new ArrayList<Event>();
	}

	public boolean addEvent(Event error) {
		try {
			return m_errors.offer(error, 10, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Cat.logError(e);
		}
		return false;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		if (!m_manager.isLocalMode()) {
			Threads.forGroup("Cat").start(new Job());
		}
	}

	public class Job implements Task {

		@Override
		public String getName() {
			return "Event-Write-Thread";
		}

		@Override
		public void run() {
			boolean active = true;
			while (active) {
				try {
					Event error = m_errors.poll(5, TimeUnit.MILLISECONDS);

					if (error != null) {
						m_eventDao.insert(error);
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
		public void shutdown() {
		}
	}

}
