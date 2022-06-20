package com.dianping.cat.message;

public interface Log {
	/**
	 * The time stamp the message was created.
	 * 
	 * @return message creation time stamp in milliseconds
	 */
	public long getTimestamp();

	public String getSeverity();

	public String getMessage();
}
