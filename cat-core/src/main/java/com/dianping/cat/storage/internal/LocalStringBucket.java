package com.dianping.cat.storage.internal;

import java.io.IOException;
import java.nio.charset.Charset;

import org.jboss.netty.buffer.ChannelBuffer;

public class LocalStringBucket extends AbstractFileBucket<String> {
	private static final String[] EMPTY = new String[0];

	@Override
	protected String decode(ChannelBuffer buf) throws IOException {
		return (String) buf.toString(buf.readerIndex(), buf.readableBytes(), Charset.forName("utf-8"));
	}

	@Override
	protected void encode(String data, ChannelBuffer buf) throws IOException {
		String str = (String) data;
		byte[] bytes = str.getBytes("utf-8");

		buf.writeInt(bytes.length);
		buf.writeBytes(bytes);
	}

	@Override
	public boolean storeById(String id, String data) {
		return storeById(id, data, EMPTY);
	}

	@Override
	protected boolean isAutoFlush() {
		return true;
	}
}
