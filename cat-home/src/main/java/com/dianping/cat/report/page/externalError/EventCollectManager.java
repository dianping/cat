package com.dianping.cat.report.page.externalError;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
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

	public static final int TEN = 10;

	@Inject
	private EventDao m_eventDao;

	@Inject
	private ServerConfigManager m_manager;

	private BlockingQueue<Event> m_errors = new LinkedBlockingQueue<Event>(1000);

	private Map<Long, Map<String, List<Event>>> m_events = new LinkedHashMap<Long, Map<String, List<Event>>>(360);

	private Logger m_logger;

	public List<Event> findOrCreateEvents(long date, String domain) {
		Map<String, List<Event>> domainEvent = m_events.get(date);

		if (domainEvent == null) {
			domainEvent = new HashMap<String, List<Event>>();
			m_events.put(date, domainEvent);
		}

		List<Event> result = domainEvent.get(domain);

		if (result == null) {
			result = new ArrayList<Event>();
			domainEvent.put(domain, result);
		}

		return result;
	}

	private List<Event> queryEventsByMemory(String domain, Date date, int minute) {
		List<Event> result = new ArrayList<Event>();
		long time = date.getTime();

		for (int i = 0; i < minute; i++) {
			List<Event> events = findOrCreateEvents(time - minute * TimeUtil.ONE_MINUTE, domain);
			result.addAll(events);
		}
		return result;
	}

	private List<Event> queryEventsByDB(String domain, Date date, int minute) {
		Date start = new Date(date.getTime() - TimeUtil.ONE_MINUTE * minute);
		Date end = new Date(date.getTime() + TimeUtil.ONE_MINUTE);

		try {
			return m_eventDao.findByDomainTime(domain, start, end, EventEntity.READSET_FULL);
		} catch (DalException e) {
			Cat.logError(e);
		}
		return new ArrayList<Event>();
	}

	public List<Event> queryEvents(String domain, Date date) {
		long current = System.currentTimeMillis();

		if (current - date.getTime() < TimeUtil.ONE_HOUR * 2) {
			return queryEventsByMemory(domain, date, 10);
		} else {
			return queryEventsByDB(domain, date, 10);
		}
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

						long date = error.getDate().getTime();
						String domain = error.getDomain();
						long time = date - date % TimeUtil.ONE_MINUTE;

						findOrCreateEvents(time, domain).add(error);
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
