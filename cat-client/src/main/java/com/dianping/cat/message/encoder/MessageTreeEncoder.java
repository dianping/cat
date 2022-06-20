package com.dianping.cat.message.encoder;

import com.dianping.cat.message.context.MessageTree;

import io.netty.buffer.ByteBuf;

public interface MessageTreeEncoder {
	public void encode(MessageTree tree, ByteBuf buf);
}
