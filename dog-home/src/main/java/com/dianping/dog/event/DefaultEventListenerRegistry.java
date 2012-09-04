package com.dianping.dog.event;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DefaultEventListenerRegistry implements EventListenerRegistry {
	
	private Map<EventType, List<EventListener<Event>>> m_map = new HashMap<EventType, List<EventListener<Event>>>();

	@Override
	public List<EventListener<Event>> getListeners(EventType type) {
		return m_map.get(type);
	}

	@SuppressWarnings("unchecked")
	@Override
	public void register(EventType type, EventListener<?> listener) {
		List<EventListener<Event>> listeners = m_map.get(type);

		if (listeners == null) {
			synchronized (m_map) {
				listeners = m_map.get(type);

				if (listeners == null) {
					listeners = new ArrayList<EventListener<Event>>();
					m_map.put(type, listeners);
				}
			}
		}

		synchronized (listeners) {
			listeners.add((EventListener<Event>) listener);
		}
	}
	
	public void modify(EventType type,EventListener<?> listener){
	}
	
	public void remove(EventType type,EventListener<?> listener){
	}
	
	//
//	@Override
//   public boolean isEligible(RuleEvent event) {
//	   return true;
//   }
//
//	@Override
//   public void onEvent(RuleEvent event) {
//		
//		
//   }
}
