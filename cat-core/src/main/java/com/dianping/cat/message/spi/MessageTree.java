package com.dianping.cat.message.spi;

import com.dianping.cat.message.Message;

public interface MessageTree {
	public String getDomain();

	public String getHostName();

	public String getIpAddress();

	public Message getMessage();

	public String getMessageId();

	public int getPort();

	public String getRequestToken();

	public String getSessionToken();

	public String getThreadId();
}
