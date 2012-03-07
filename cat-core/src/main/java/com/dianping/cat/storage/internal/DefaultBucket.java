package com.dianping.cat.storage.internal;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;

public class DefaultBucket<T> extends AbstractFileBucket<T> {
	private Class<?> m_type;

	@SuppressWarnings("unchecked")
	@Override
	protected T decode(ChannelBuffer buf) throws IOException {
		if (m_type == String.class) {
			return (T) buf.toString(buf.readerIndex(), buf.readableBytes(), Charset.forName("utf-8"));
		} else if (m_type == byte[].class) {
			byte[] bytes = new byte[buf.readableBytes()];

			buf.readBytes(bytes);

			return (T) bytes;
		} else {
			throw new UnsupportedOperationException(String.format(
			      "Only String or byte[] are supported so far, but was %s.", m_type));
		}
	}

	@Override
	protected void encode(T data, ChannelBuffer buf) throws IOException {
		if (m_type == String.class) {
			String str = (String) data;
			byte[] bytes = str.getBytes("utf-8");

			buf.writeInt(bytes.length);
			buf.writeBytes(bytes);
		} else if (m_type == byte[].class) {
			byte[] bytes = (byte[]) data;

			buf.writeInt(bytes.length);
			buf.writeBytes(bytes);
		} else {
			throw new UnsupportedOperationException(String.format(
			      "Only String or byte[] are supported so far, but was %s.", m_type));
		}
	}

	@Override
	public void initialize(Class<?> type, File path) throws IOException {
		super.initialize(type, path);

		m_type = type;

		if (m_type != String.class && m_type != byte[].class) {
			throw new UnsupportedOperationException(String.format(
			      "Only String or byte[] are supported so far, but was %s.", m_type));
		}
	}

	@Override
   protected boolean isAutoFlush() {
	   return true;
   }
}
