package com.dianping.dog.event;

public interface EventListener{
	public boolean isEligible(Event event);

	public void onEvent(Event event);
}
