package com.dianping.cat.message.spi;

import org.jboss.netty.buffer.ChannelBuffer;

public interface MessageCodec {
	public MessageTree decode(ChannelBuffer buf);

	public void decode(ChannelBuffer buf, MessageTree tree);

	public void encode(MessageTree tree, ChannelBuffer buf);
}
