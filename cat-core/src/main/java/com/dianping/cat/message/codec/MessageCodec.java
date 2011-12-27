package com.dianping.cat.message.codec;

import com.dianping.cat.message.Message;

public interface MessageCodec {
	public byte[] encode(Message message);

	public Message decode(byte[] data);
}
