package com.dianping.dog.alarm.notification;

import java.util.List;

import com.dianping.dog.event.Event;

public interface EmailEvent  extends Event{
	public String getTitle();

	public String getContent();

	public List<String> getTos();
}
