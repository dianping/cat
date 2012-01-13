package com.dianping.cat.message.spi.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DefaultMessageStorage implements MessageStorage, LogEnabled {
	@Inject
	private MessagePathBuilder m_builder;

	@Inject
	private MessageCodec m_codec;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public File getBaseDir() {
		return m_builder.getLogViewBaseDir();
	}

	@Override
	public URL getBaseUrl() {
		return m_builder.getLogViewBaseUrl();
	}

	@Override
	public String store(MessageTree tree) {
		String path = m_builder.getLogViewPath(tree);
		File file = new File(m_builder.getLogViewBaseDir(), path);
		ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);
		FileOutputStream fos = null;

		file.getParentFile().mkdirs();

		try {
			m_codec.encode(tree, buf); 
			fos = new FileOutputStream(file);
			buf.getBytes(buf.readerIndex(), fos, buf.readableBytes());
		} catch (IOException e) {
			m_logger.error(String.format("Error when writing to file(%s)!", file), e);
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
					// ignore it
				}
			}
		}

		return path;
	}
}
