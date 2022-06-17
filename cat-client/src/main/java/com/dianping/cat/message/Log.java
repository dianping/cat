package com.dianping.cat.message;

public interface Log {
	public long getTimestamp();

	public String getSeverity();

	public String getMessage();
}
