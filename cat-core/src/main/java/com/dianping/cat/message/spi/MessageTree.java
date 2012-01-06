package com.dianping.cat.message.spi;

import com.dianping.cat.message.Message;

public interface MessageTree {
	public String getDomain();

	public String getHostName();

	public String getIpAddress();

	public Message getMessage();

	public String getMessageId();

	public String getRequestToken();

	public String getSessionToken();

	public String getThreadId();

	public void setDomain(String domain);

	public void setHostName(String hostName);

	public void setIpAddress(String ipAddress);

	public void setMessage(Message message);

	public void setMessageId(String messageId);

	public void setRequestToken(String requestToken);

	public void setSessionToken(String sessionToken);

	public void setThreadId(String threadId);
}
