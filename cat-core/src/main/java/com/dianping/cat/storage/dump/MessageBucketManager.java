package com.dianping.cat.storage.dump;

import java.io.IOException;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

public interface MessageBucketManager {
	public void close() throws IOException;

	public MessageTree loadMessage(String messageId) throws IOException;

	public void storeMessage(MessageTree tree,MessageId id) throws IOException;
}
