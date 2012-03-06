package com.dianping.cat.job.hdfs;

import java.io.IOException;
import java.io.InputStream;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.message.spi.internal.DefaultMessageTree;
import com.site.lookup.annotation.Inject;

public class DefaultInputChannel implements InputChannel {
	@Inject
	private MessageCodec m_codec;


	private InputStream m_in;



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

	@Override
	public void initialize(InputStream in) {
		m_in = in;
	}

	@Override
	public MessageTree read(int index, int length) throws IOException {
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
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
}
