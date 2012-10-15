package com.dianping.cat.system.event;

public interface EventListener{
	public boolean isEligible(Event event);

	public void onEvent(Event event);
}
