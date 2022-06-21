package com.dianping.cat.message;

import java.util.List;

public interface LogSegment {
	String getDomain();

	String getHostName();

	String getIpAddress();

	List<Log> getLogs();
}
