package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;
import java.io.OutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.configuration.server.entity.HdfsConfig;
import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DefaultOutputChannel implements OutputChannel {
	@Inject
	private MessageCodec m_codec;

	@Inject
	private long m_ttl = 90 * 1000L; // 90 seconds

	private OutputStream m_out;

	private int m_maxSize;

	private int m_count;

	private long m_timestamp;

	@Override
	public void close() {
		if (m_out != null) {
			try {
				m_out.flush();
				m_out.close();
				m_out = null;
			} catch (IOException e) {
				// ignore it
			}
		}
	}

	@Override
	public int getSize() {
		return m_count;
	}

	@Override
	public void initialize(HdfsConfig config, OutputStream out) {
		m_out = out;
		m_timestamp = System.currentTimeMillis();
		m_maxSize = toInteger(config.getMaxSize(), 0);
	}

	@Override
	public boolean isExpired() {
		long now = System.currentTimeMillis();

		return now - m_timestamp > m_ttl;
	}

	public void setMaxSize(int maxSize) {
		m_maxSize = maxSize;
	}

	public void setTtl(long ttl) {
		m_ttl = ttl;
	}

	int toInteger(String str, int defaultValue) {
		int value = 0;
		int len = str.length();

		for (int i = 0; i < len; i++) {
			char ch = str.charAt(i);

			if (Character.isDigit(ch)) {
				value = value * 10 + (ch - '0');
			} else if (ch == 'm' || ch == 'M') {
				value *= 1024 * 1024;
			} else if (ch == 'k' || ch == 'K') {
				value *= 1024;
			}
		}

		if (value > 0) {
			return value;
		} else {
			return defaultValue;
		}
	}

	@Override
	public int write(MessageTree tree) throws IOException {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

		m_codec.encode(tree, buf);

		int length = buf.readInt();

		if (m_maxSize > 0 && m_count + length + 1 > m_maxSize) {
			// exceed the max size
			return 0;
		}

		buf.getBytes(buf.readerIndex(), m_out, length);

		// a blank line used to separate two message trees
		m_out.write('\n');
		m_out.flush();
		m_count += length + 1;

		return length + 1;
	}
}
