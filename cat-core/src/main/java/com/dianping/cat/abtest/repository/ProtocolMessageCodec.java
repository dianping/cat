package com.dianping.cat.abtest.repository;

import java.io.UnsupportedEncodingException;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.unidal.socket.MessageCodec;

public class ProtocolMessageCodec implements MessageCodec<ProtocolMessage> {
	@Override
	public ProtocolMessage decode(ChannelBuffer buf) {
		ProtocolMessage message = new ProtocolMessage();

		buf.readInt(); // get rid of the place-holder
		message.setName(readString(buf));
		message.setContent(readString(buf));

		while (buf.readable()) {
			String key = readString(buf);
			String value = readString(buf);

			message.getHeaders().put(key, value);
		}

		return message;
	}

	@Override
	public ChannelBuffer encode(ProtocolMessage message) {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(4096);

		buf.writeInt(0); // place-holder
		writeString(buf, message.getName());
		writeString(buf, message.getContent());

		for (Map.Entry<String, String> e : message.getHeaders().entrySet()) {
			writeString(buf, e.getKey());
			writeString(buf, e.getValue());
		}

		return buf;
	}

	private String readString(ChannelBuffer buf) {
		int length = buf.readShort();

		if (length == -1) {
			return null;
		} else {
			byte[] bytes = new byte[length];

			buf.readBytes(bytes);

			try {
				return new String(bytes, "utf-8");
			} catch (UnsupportedEncodingException e) {
				return new String(bytes);
			}
		}
	}

	private void writeString(ChannelBuffer buf, String str) {
		byte[] bytes = null;

		if (str != null) {
			try {
				bytes = str.getBytes("utf-8");
			} catch (UnsupportedEncodingException e) {
				bytes = str.getBytes();
			}
		}

		if (bytes == null) {
			buf.writeShort(-1);
		} else {
			buf.writeShort(bytes.length);
			buf.writeBytes(bytes);
		}
	}
}