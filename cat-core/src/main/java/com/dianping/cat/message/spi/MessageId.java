package com.dianping.cat.message.spi;

public interface MessageId {
	public String getDomain();

	public long getTimestamp();
}
