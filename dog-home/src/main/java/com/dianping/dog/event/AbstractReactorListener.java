package com.dianping.dog.event;

import com.site.lookup.annotation.Inject;

public abstract class AbstractReactorListener <T extends Event> implements EventListener<T> {

	@Inject
	protected EventDispatcher m_dispatcher;
	
	@Override
   public boolean isEligible(T event) {
	   return true;
   }

	public void setDispatcher(EventDispatcher dispatcher) {
   	this.m_dispatcher = dispatcher;
   }
	   
}
