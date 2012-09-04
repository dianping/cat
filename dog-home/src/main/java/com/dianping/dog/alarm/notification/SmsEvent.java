package com.dianping.dog.alarm.notification;

import java.util.List;

public interface SmsEvent {
	public String getContent();

	public List<String> getPhoneNumbers();
}
