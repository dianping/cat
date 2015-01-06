package com.dianping.cat.message.spi.codec;

import io.netty.buffer.ByteBuf;

public interface BufferWriter {
	public int writeTo(ByteBuf buf, byte[] data);
}
