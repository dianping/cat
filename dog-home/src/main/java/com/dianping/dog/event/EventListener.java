package com.dianping.dog.event;

public interface EventListener<T extends Event> {

	public boolean isEligible(T event);

	public void onEvent(T event);

}
