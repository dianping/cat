package com.dianping.cat.system.event;

import java.util.ArrayList;
import java.util.List;

public class DefaultEventListenerRegistry implements EventListenerRegistry {
	private List<EventListener> m_listeners = new ArrayList<EventListener>();

	@Override
	public List<EventListener> getListeners() {
		List<EventListener> listeners = new ArrayList<EventListener>();
		synchronized (m_listeners) {
			for (EventListener listener : m_listeners) {
				listeners.add(listener);
			}
		}
		return listeners;
	}

	@Override
	public void register(EventListener listener) {
		synchronized (m_listeners) {
			m_listeners.add(listener);
		}
	}

}
