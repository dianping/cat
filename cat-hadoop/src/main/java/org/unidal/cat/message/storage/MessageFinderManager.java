package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.internal.MessageId;

public interface MessageFinderManager {
	public void close(int hour);

	public ByteBuf find(MessageId id);

	public void register(int hour, MessageFinder finder);
}
