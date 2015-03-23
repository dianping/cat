package com.dianping.cat.message.storage;

import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessageTree;

public interface MessageBucketManager {
	public MessageTree loadMessage(String messageId);

	public void storeMessage(MessageTree tree, MessageId id);
}
