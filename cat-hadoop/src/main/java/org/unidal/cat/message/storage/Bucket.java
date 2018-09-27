package org.unidal.cat.message.storage;

import io.netty.buffer.ByteBuf;

import java.io.IOException;
import java.util.Map;

import com.dianping.cat.message.internal.MessageId;

public interface Bucket {
	public static final long SEGMENT_SIZE = 32 * 1024L;

	public static final int BYTE_PER_MESSAGE = 8;

	public static final int BYTE_PER_ENTRY = 8;

	public static final int MESSAGE_PER_SEGMENT = (int) (SEGMENT_SIZE / BYTE_PER_MESSAGE);

	public static final int ENTRY_PER_SEGMENT = (int) (SEGMENT_SIZE / BYTE_PER_ENTRY);

	public void close();

	public void flush();

	public ByteBuf get(MessageId id) throws IOException;

	public boolean initialize(String domain, String ip, int hour, boolean writeMode) throws IOException;

	public void puts(ByteBuf buf, Map<MessageId, Integer> mappings) throws IOException;
}
