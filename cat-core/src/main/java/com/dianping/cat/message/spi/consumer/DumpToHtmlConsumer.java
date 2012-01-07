package com.dianping.cat.message.spi.consumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DumpToHtmlConsumer implements MessageConsumer {
	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessagePathBuilder m_pathBuilder;

	@Override
	public void consume(MessageTree tree) {
		File file = m_pathBuilder.getLogViewFile(tree);
		FileOutputStream fos = null;

		try {
			ChannelBuffer buf = ChannelBuffers.buffer(8192);

			m_codec.encode(tree, buf);
			fos = new FileOutputStream(file);
			buf.getBytes(buf.readerIndex(), fos, buf.readableBytes());
		} catch (Exception e) {
			throw new RuntimeException(String.format("Error when dumping to HTML file(%s)!", file), e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// ignore it
				}
			}
		}

	}

	@Override
	public String getConsumerId() {
		return "dump-to-file";
	}

	@Override
	public String getDomain() {
		// no limitation
		return null;
	}
}
