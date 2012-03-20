package com.dianping.cat.storage.internal;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Date;

import org.jboss.netty.buffer.ChannelBuffer;

import com.dianping.cat.message.spi.MessagePathBuilder;
import com.site.lookup.annotation.Inject;

public class LocalStringBucket extends AbstractFileBucket<String> {
	private static final String[] EMPTY = new String[0];

	@Inject
	private MessagePathBuilder m_pathBuilder;

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

	@Override
	protected String getLogicalPath(Date timestamp, String name) {
		return m_pathBuilder.getReportPath(name, timestamp);
	}
}
