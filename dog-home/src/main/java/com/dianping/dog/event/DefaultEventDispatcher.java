package com.dianping.dog.event;

import java.util.List;

import com.dianping.cat.Cat;
import com.site.lookup.annotation.Inject;

public class DefaultEventDispatcher implements EventDispatcher {
	@Inject
	private EventListenerRegistry m_registry;

	@Override
	public void dispatch(Event event) {
		List<EventListener<Event>> listeners = m_registry.getListeners(event.getEventType());

		if (listeners != null && !listeners.isEmpty()) {
			for (EventListener<Event> listener : listeners) {
				try {
						listener.onEvent(event);
				} catch (Exception e) {
					// ignore it
					Cat.getProducer().logError(e);
				}
			}
		} else {
			Cat.getProducer().logEvent("UnhandledEvent", event.getEventType().getClass().getName(), "0", null);
		}
	}
}
