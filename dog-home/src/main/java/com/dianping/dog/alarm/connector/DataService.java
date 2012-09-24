package com.dianping.dog.alarm.connector;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.Cat;
import com.dianping.dog.alarm.problem.ProblemDataEvent;
import com.dianping.dog.event.DefaultEventDispatcher;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventType;
import com.dianping.dog.lifecycle.LifeCycle;
import com.dianping.dog.lifecycle.LifeCycleException;
import com.site.helper.Threads;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DataService extends ContainerHolder implements LifeCycle, Runnable, LogEnabled {
	@Inject
	private ConnectorManager m_connectorMananger;

	@Inject
	private EventDispatcher m_eventDispatcher;

	private static final long SLEEP_TIME = 5 * 1000;// sleep for five seconds

	private AtomicBoolean m_active = new AtomicBoolean();

	private Logger m_logger;

	@Override
	public void init() throws LifeCycleException {
		if (m_connectorMananger == null || m_eventDispatcher == null) {
			throw new LifeCycleException("can not find component:" + ConnectorManager.class.getName() + " or "
			      + DefaultEventDispatcher.class.getName());
		}
		m_active.set(true);
	}

	@Override
	public void start() throws LifeCycleException {
		Threads.forGroup("Dog-DataService").start(this);
	}

	@Override
	public void run() {
		while (m_active.get()) {
			try {
				Date currentTime = new Date(System.currentTimeMillis());// MilliSecondTimer.currentTimeMicros();
				List<Connector> connectors = m_connectorMananger.getConnectors();
				for (Connector con : connectors) {
					try {
						RowData data = con.produceData(currentTime);
						if (data == null) {
							continue;
						}
						Event event = null;
						if (data.getType() == EventType.ProblemDataEvent) {
							event = new ProblemDataEvent(data);
						}
						if (event != null) {
							m_logger.debug("RowData:" + data);
							m_eventDispatcher.dispatch(event);
						}
					} catch (Exception ex) {
						m_logger.error(ex.getMessage());
					}
				}
			} catch (Exception e) {
				m_logger.error(e.getMessage());
				Cat.logError(e);
			}
			try {
				Thread.sleep(SLEEP_TIME);
			} catch (Exception e) {
				m_logger.error(String.format("fail to sleep for [Time]:%s ,[error]: %s", SLEEP_TIME, e.getMessage()));
			}
		}
	}

	@Override
	public void stop() throws LifeCycleException {
		m_active.set(false);
	}

	@Override
	public void enableLogging(Logger logger) {
		this.m_logger = logger;
	}

}
