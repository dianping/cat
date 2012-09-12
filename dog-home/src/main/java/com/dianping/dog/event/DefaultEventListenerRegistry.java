package com.dianping.dog.event;

import java.util.ArrayList;
import java.util.List;

public class DefaultEventListenerRegistry implements EventListenerRegistry {
	private List<EventListener<Event>> m_listeners = new ArrayList<EventListener<Event>>();

	@Override
	public synchronized List<EventListener<Event>> getListeners() {
		List<EventListener<Event>> listeners = new ArrayList<EventListener<Event>>();
		for (EventListener<Event> listener : m_listeners) {
			listeners.add(listener);
		}
		return listeners;
	}

	@SuppressWarnings("unchecked")
   @Override
	public synchronized void register(EventListener<? extends Event> listener) {
		if (m_listeners == null) {
			m_listeners = new ArrayList<EventListener<Event>>();
		}
		m_listeners.add((EventListener<Event>) listener);
	}

}
