package com.dianping.cat.system.alarm;

import com.dianping.cat.system.alarm.connector.Connector;
import com.dianping.cat.system.alarm.exception.ExceptionDataEntity;
import com.dianping.cat.system.alarm.exception.ExceptionRuleManager;
import com.dianping.cat.system.alarm.exception.event.ExceptionDataEvent;
import com.dianping.cat.system.event.EventDispatcher;
import com.site.helper.Threads.Task;
import com.site.lookup.annotation.Inject;

public class ExceptionAlarmTask implements Task {

	@Inject
	private ExceptionRuleManager m_manager;

	@Inject
	private Connector m_connector;

	@Inject
	private EventDispatcher m_dispatcher;

	@Override
	public void run() {
		while (true) {
			long time = System.currentTimeMillis();

			try {
				ExceptionDataEvent event = new ExceptionDataEvent();
				event.setData(new ExceptionDataEntity());
				m_dispatcher.dispatch(event);
			} catch (Exception e) {

			}
			long duration = System.currentTimeMillis() - time;
			try {
				Thread.sleep(3 * 1000 - duration);
			} catch (Exception e) {
				// igrone
			}
		}
	}

	@Override
	public String getName() {
		return "ExceptionAlarm";
	}

	@Override
	public void shutdown() {
	}

}
