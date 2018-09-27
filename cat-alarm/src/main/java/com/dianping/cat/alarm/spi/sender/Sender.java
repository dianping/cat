package com.dianping.cat.alarm.spi.sender;


public interface Sender {

	public String getId();

	public boolean send(SendMessageEntity message);

}
