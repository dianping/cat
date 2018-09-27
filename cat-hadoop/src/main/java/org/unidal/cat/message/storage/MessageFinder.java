package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.internal.MessageId;

public interface MessageFinder {
	public ByteBuf find(MessageId id);
}
