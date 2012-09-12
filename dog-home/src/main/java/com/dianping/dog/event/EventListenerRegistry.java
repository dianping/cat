package com.dianping.dog.event;

import java.util.List;

public interface EventListenerRegistry{
	
	public  List<EventListener<Event>> getListeners();

	public void register(EventListener<? extends Event> listener);
	
}
