package com.dianping.dog.event;

import java.util.ArrayList;
import java.util.List;

public class DefaultEventListenerRegistry implements EventListenerRegistry {
	private List<EventListener> m_listeners = new ArrayList<EventListener>();

	@Override
	public synchronized List<EventListener> getListeners() {
		List<EventListener> listeners = new ArrayList<EventListener>();
		for (EventListener listener : m_listeners) {
			listeners.add(listener);
		}
		return listeners;
	}

   @Override
	public synchronized void register(EventListener listener) {
		if (m_listeners == null) {
			m_listeners = new ArrayList<EventListener>();
		}
		m_listeners.add((EventListener) listener);
	}

}
