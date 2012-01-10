package com.dianping.cat.message.spi.codec;

import org.jboss.netty.buffer.ChannelBuffer;

public interface BufferWriter {
	public int writeTo(ChannelBuffer buf, byte[] data);
}
