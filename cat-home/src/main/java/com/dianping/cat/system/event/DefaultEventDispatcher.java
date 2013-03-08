package com.dianping.cat.system.event;

import java.util.List;

import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;

public class DefaultEventDispatcher implements EventDispatcher {
	@Inject
	private EventListenerRegistry m_registry;

	@Override
	public void dispatch(Event event) {
		List<EventListener> listeners = m_registry.getListeners();

		if (listeners != null && !listeners.isEmpty()) {
			for (EventListener listener : listeners) {
				try {
					if (listener.isEligible(event)) {
						listener.onEvent(event);
					}
				} catch (Exception e) {
					Cat.getProducer().logError(e);
				}
			}
		} else {
			Cat.getProducer().logEvent("UnhandledEvent", event.getEventType().getClass().getName(), "0", null);
		}
	}
}
