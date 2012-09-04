package com.dianping.dog.event;

import java.util.List;

public interface EventListenerRegistry{
	
	public List<EventListener<Event>> getListeners(EventType type);

	public void register(EventType type, EventListener<?> listener);
	
}
