package com.dianping.dog.alarm.notification;

import java.util.List;

public interface EmailEvent {
	public String getTitle();

	public String getContent();

	public List<String> getTos();
}
