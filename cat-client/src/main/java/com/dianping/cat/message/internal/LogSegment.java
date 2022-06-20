package com.dianping.cat.message.internal;

import java.util.List;

import com.dianping.cat.message.Log;

public interface LogSegment {
	String getDomain();

	String getHostName();

	String getIpAddress();

	List<Log> getLogs();
}
