package com.dianping.dog.alarm.data;

import java.util.Date;

public interface DataEvent {
	public Date getTimestamp();

	public String getDomain();

	public String getIp();

	public String getReport();

	public String getType();

	public String getName();

	public long getTotalCount();

	public long getFailCount();
}
