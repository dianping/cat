package com.dianping.cat.alarm.server;

import org.unidal.helper.Threads.Task;

public interface ServerAlarm extends Task {

	public String getCategory();

	public String getID();

}
