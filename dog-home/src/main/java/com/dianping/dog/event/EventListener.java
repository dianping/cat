package com.dianping.dog.event;

public interface EventListener<T extends Event> {
	public void onEvent(T event);
}
