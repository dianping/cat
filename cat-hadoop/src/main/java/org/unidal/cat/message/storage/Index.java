package org.unidal.cat.message.storage;

import java.io.IOException;
import java.util.Map;

import com.dianping.cat.message.internal.MessageId;

public interface Index {

	public void close();

	public MessageId find(MessageId from) throws IOException;

	public void initialize(String domain, String ip, int hour) throws IOException;

	public void map(MessageId from, MessageId to) throws IOException;

	public void maps(Map<MessageId, MessageId> maps) throws IOException;
}
