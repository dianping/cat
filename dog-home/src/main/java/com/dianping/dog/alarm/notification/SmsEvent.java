package com.dianping.dog.alarm.notification;

import java.util.List;

import com.dianping.dog.event.Event;

public interface SmsEvent  extends Event{
	public String getContent();

	public List<String> getPhoneNumbers();
}
