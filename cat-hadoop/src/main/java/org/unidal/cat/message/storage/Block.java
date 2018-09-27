package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Map;

import com.dianping.cat.message.internal.MessageId;

public interface Block {
	public void clear();

	public ByteBuf find(MessageId id);

	public void finish();

	public ByteBuf getData() throws IOException;

	public String getDomain();

	public int getHour();

	public Map<MessageId, Integer> getOffsets();

	public boolean isFull();

	public void pack(MessageId id, ByteBuf buf) throws IOException;

	public ByteBuf unpack(MessageId id) throws IOException;

}
