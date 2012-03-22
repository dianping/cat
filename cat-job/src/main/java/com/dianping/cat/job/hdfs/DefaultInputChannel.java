package com.dianping.cat.job.hdfs;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.annotation.Inject;

public class DefaultInputChannel implements InputChannel {
	@Inject
	private MessageCodec m_codec;

	private FSDataInputStream m_in;
	
	private String path;

	@Override
	public void close() {
		if (m_in != null) {
			try {
				m_in.close();
				m_in = null;
			} catch (IOException e) {
				// ignore it
			}
		}
	}

	public void initialize(FSDataInputStream in) {
		m_in = in;
	}

	@Override
	public MessageTree read(long offset, int length) throws IOException {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

		m_in.seek(offset);
		buf.writeBytes(m_in, length);

		MessageTree tree = new DefaultMessageTree();

		m_codec.decode(buf, tree);
		return tree;
	}

	@Override
	public boolean isExpired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}
}
