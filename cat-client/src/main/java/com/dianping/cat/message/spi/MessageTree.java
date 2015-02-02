package com.dianping.cat.message.spi;

import com.dianping.cat.message.Message;

public interface MessageTree extends Cloneable {
	public MessageTree copy();

	public String getDomain();

	public String getHostName();

	public String getIpAddress();

	public Message getMessage();

	public String getMessageId();

	public String getParentMessageId();

	public String getRootMessageId();

	public String getSessionToken();

	public String getThreadGroupName();

	public String getThreadId();

	public String getThreadName();

	public void setDomain(String domain);

	public void setHostName(String hostName);

	public void setIpAddress(String ipAddress);

	public void setMessage(Message message);

	public void setMessageId(String messageId);

	public void setParentMessageId(String parentMessageId);

	public void setRootMessageId(String rootMessageId);

	public void setSessionToken(String sessionToken);

	public void setThreadGroupName(String name);

	public void setThreadId(String threadId);

	public void setThreadName(String id);
}
