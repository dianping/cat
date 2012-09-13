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
import com.site.lookup.annotation.Inject;

public class DataService extends ContainerHolder implements LifeCycle {

	@Inject
	private ConnectorManager m_connectorMananger;

	@Inject
	private EventDispatcher m_eventDispatcher;

	private static final long SLEEP_TIME = 5 * 1000;// sleep for five seconds

	private AtomicBoolean m_active = new AtomicBoolean();

	private Thread serviceTask = null;

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
		serviceTask = new Thread(new Runnable() {
			public void run() {
				doService();
			}
		}, "DataService-Thread");
		serviceTask.start();
	}

	protected void doService() {
		try {
			while (m_active.get()) {
				Date currentTime = new Date(System.currentTimeMillis());// MilliSecondTimer.currentTimeMicros();
				List<Connector> connectors = m_connectorMananger.getConnectors();
				try {
					for (Connector con : connectors) {
						try {
							RowData data = con.produceData(currentTime);
							if (data == null) {
								continue;
							}
							Event event = null;
							if (data.getType() == EventType.ProblemEvent) {
								event = new ProblemEvent(data);
							}
							if (event != null) {
								m_eventDispatcher.dispatch(event);
							}
						} catch (Exception ex) {
							System.out.println(ex.toString());
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
