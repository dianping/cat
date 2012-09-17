package com.dianping.dog.alarm.data;

import com.dianping.dog.event.Event;

public interface DataEvent extends Event{
	public long getTimestamp();

	public String getDomain();

	public String getIp();

	public String getReport();

	public String getType();

	public String getName();

	public long getTotalCount();

	public long getFailCount();
	
}
