package com.dianping.cat.message.spi;

public interface MessageTree {
	public String getDomain();

	public String getHostName();

	public String getIpAddress();

	public MessageTree getMessage();

	public String getMessageId();

	public int getPort();

	public String getRequestToken();

	public String getSessionToken();

	public String getThreadId();
}
