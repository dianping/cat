package com.dianping.cat.message.spi;

import io.netty.buffer.ByteBuf;

public interface MessageCodec {
	/**
		* decode buf to message tree
		* first 4 bytes is the length of message tree
		*
		* @param buf
		* @return message
		*/
	public MessageTree decode(ByteBuf buf);

	/**
		* encode message tree to buf
		*
		* @param tree
		* @return buf  first 4 bytes is the length of message tree
		*/
	public ByteBuf encode(MessageTree tree);

	public void reset();
}
