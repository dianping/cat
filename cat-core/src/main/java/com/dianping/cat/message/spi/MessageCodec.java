package com.dianping.cat.message.spi;

public interface MessageCodec {
	public byte[] encode(MessageTree tree);

	public MessageTree decode(byte[] data);
}
