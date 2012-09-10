package com.dianping.dog.alarm.connector;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.dianping.dog.alarm.problem.ProblemEvent;
import com.dianping.dog.event.DefaultEventDispatcher;
import com.dianping.dog.event.Event;
import com.dianping.dog.event.EventDispatcher;
import com.dianping.dog.event.EventType;
import com.dianping.dog.lifecycle.LifeCycle;
import com.dianping.dog.lifecycle.LifeCycleException;
import com.site.lookup.ContainerHolder;

public class DataService extends ContainerHolder implements LifeCycle {

	private ConnectorManager m_connectorMananger;

	private EventDispatcher m_dispatcher;

	private static final long SLEEP_TIME = 5 * 1000;// sleep for five seconds

	private AtomicBoolean m_active = new AtomicBoolean();

	private Thread serviceTask = null;

	@Override
	public void init() throws LifeCycleException {
		m_connectorMananger = lookup(ConnectorManager.class);
		m_dispatcher = lookup(DefaultEventDispatcher.class);
		if (m_connectorMananger == null || m_dispatcher == null) {
			throw new LifeCycleException("can not find component:" + ConnectorManager.class.getName() + " or "
			      + DefaultEventDispatcher.class.getName());
		}
	}

	@Override
	public void start() throws LifeCycleException {
		serviceTask = new Thread(new Runnable() {
			public void run() {
				doService();
			}
		}, "DataService-Thread");
		serviceTask.start();
	}

	private void doService() {
		try {
			while (m_active.get()) {
				Date currentTime = new Date(System.currentTimeMillis());// MilliSecondTimer.currentTimeMicros();
				List<Connector> connectors = m_connectorMananger.getConnectors();
				try {
					for (Connector con : connectors) {
						try {
							RowData data = con.produceData(currentTime);
							Event event = null;
							if (data.getType() == EventType.ProblemEvent) {
								event = new ProblemEvent(data);
							}
							if (event != null) {
								m_dispatcher.dispatch(event);
							}
						} catch (Exception ex) {
							// TODO logger
						}
					}
				} catch (Exception ex) {
					// TODO logger
				}
				Thread.sleep(SLEEP_TIME);
			}
		} catch (InterruptedException e) {
		    // TODO logger
		}
	}

	@Override
	public void stop() throws LifeCycleException {
		m_active.set(false);
	}

}
