package com.dianping.cat.message.spi;

public interface MessageHandler {
	public void handle(MessageTree message);
}
