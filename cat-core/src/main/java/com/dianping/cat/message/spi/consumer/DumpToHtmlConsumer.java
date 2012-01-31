package com.dianping.cat.message.spi.consumer;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;

import com.dianping.cat.message.spi.MessageCodec;
import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DumpToHtmlConsumer implements MessageConsumer, Initializable, LogEnabled {
	@Inject
	private MessageCodec m_codec;

	@Inject
	private MessagePathBuilder m_builder;

	private Logger m_logger;

	@Override
	public void consume(MessageTree tree) {
		File baseDir = m_builder.getLogViewBaseDir();
		File file = new File(baseDir, m_builder.getLogViewPath(tree));
		FileOutputStream fos = null;

		file.getParentFile().mkdirs();

		try {
			ChannelBuffer buf = ChannelBuffers.dynamicBuffer(8192);

			m_codec.encode(tree, buf);

			int length = buf.readInt();

			fos = new FileOutputStream(file);
			buf.getBytes(buf.readerIndex(), fos, length);
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
	public void enableLogging(Logger logger) {
		m_logger = logger;
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

	@Override
	public void initialize() throws InitializationException {
		File baseDir = m_builder.getLogViewBaseDir();

		baseDir.mkdirs();
		m_logger.info(String.format("Message will be dumpped to %s in HTML.", baseDir));
	}
}
