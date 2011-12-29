package com.dianping.cat.message.spi.codec;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;

public class PlainTextMessageCodec implements MessageCodec {
	@Override
	public byte[] encode(MessageTree tree) {
		return null;
	}

	@Override
	public MessageTree decode(byte[] data) {
		return null;
	}
}
