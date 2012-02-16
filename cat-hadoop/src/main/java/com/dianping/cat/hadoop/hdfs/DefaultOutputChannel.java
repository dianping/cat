package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;
import java.io.OutputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DefaultOutputChannel implements OutputChannel {
	@Inject
	private MessageCodec m_codec;

	@Inject
	private int m_maxSize = 0; // 0 means unlimited

	@Inject
	private long m_ttl = 90 * 1000L; // 90 seconds

	private OutputStream m_out;

	private int m_count;

	private long m_timestamp;

	@Override
	public void close() {
		if (m_out != null) {
			try {
				m_out.close();
				m_out = null;
			} catch (IOException e) {
				// ignore it
			}
		}
	}

	@Override
	public void initialize(OutputStream out) {
		m_out = out;
		m_timestamp = System.currentTimeMillis();
	}

	@Override
	public boolean isExpired() {
		long now = System.currentTimeMillis();

		return now - m_timestamp > m_ttl;
	}

	@Override
	public boolean out(MessageTree tree) throws IOException {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

		m_codec.encode(tree, buf);

		int length = buf.readInt();

		if (m_maxSize > 0 && m_count + length + 1 > m_maxSize) {
			// exceed the max size
			return false;
		}

		buf.getBytes(buf.readerIndex(), m_out, length);

		// a blank line used to separate two message trees
		m_out.write('\n');
		m_count += length + 1;

		return true;
	}

	public void setMaxSize(int maxSize) {
		m_maxSize = maxSize;
	}

	public void setTtl(long ttl) {
		m_ttl = ttl;
	}
}
