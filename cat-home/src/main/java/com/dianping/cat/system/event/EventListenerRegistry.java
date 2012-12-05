package com.dianping.cat.system.event;

import java.util.List;

public interface EventListenerRegistry{
	
	public  List<EventListener> getListeners();

	public void register(EventListener listener);
	
}
