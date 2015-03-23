package com.dianping.cat.message.storage;

import java.io.IOException;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageBucket {
	public void close() throws IOException;
	
	public MessageTree findById(String messageId) throws IOException;

	public long getLastAccessTime();

	public void initialize(String dataFile) throws IOException;

}
