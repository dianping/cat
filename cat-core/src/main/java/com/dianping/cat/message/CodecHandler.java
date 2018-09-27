package com.dianping.cat.message;

import io.netty.buffer.ByteBuf;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.codec.NativeMessageCodec;
import com.dianping.cat.message.spi.codec.PlainTextMessageCodec;

public class CodecHandler {

	private static MessageCodec m_plainTextCodec = new PlainTextMessageCodec();

	private static MessageCodec m_nativeCodec = new NativeMessageCodec();

	public static MessageTree decode(ByteBuf buf) {
		byte[] data = new byte[3];
		MessageTree tree = null;

		buf.getBytes(4, data);
		String hint = new String(data);

		buf.resetReaderIndex();

		if ("PT1".equals(hint)) {
			tree = m_plainTextCodec.decode(buf);
		} else if ("NT1".equals(hint)) {
			tree = m_nativeCodec.decode(buf);
		} else {
			throw new RuntimeException("Error message type : " + hint);
		}

		MessageTreeFormat.format(tree);
		return tree;
	}

	public static void reset() {
		m_plainTextCodec.reset();
		m_nativeCodec.reset();
	}

}
