package com.dianping.cat.message.spi;

import io.netty.buffer.ByteBuf;

public interface MessageCodec {
	public MessageTree decode(ByteBuf buf);

	public void decode(ByteBuf buf, MessageTree tree);

	public void encode(MessageTree tree, ByteBuf buf);
}
