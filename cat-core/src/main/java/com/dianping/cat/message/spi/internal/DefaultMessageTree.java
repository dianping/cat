package com.dianping.cat.message.spi.internal;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.spi.MessageTree;

public class DefaultMessageTree implements MessageTree {
	@Override
	public String getDomain() {
		return null;
	}

	@Override
	public String getHostName() {
		return null;
	}

	@Override
	public String getIpAddress() {
		return null;
	}

	@Override
	public Message getMessage() {
		return null;
	}

	@Override
	public String getMessageId() {
		return null;
	}

	@Override
	public int getPort() {
		return 0;
	}

	@Override
	public String getRequestToken() {
		return null;
	}

	@Override
	public String getSessionToken() {
		return null;
	}

	@Override
	public String getThreadId() {
		return null;
	}

	public void setMessage(Message message) {
	   
   }
}
