package com.dianping.cat.message.spi.consumer;

import java.io.File;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessageConsumer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageStorage;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DumpToHtmlConsumer implements MessageConsumer, Initializable, LogEnabled {
	@Inject
	private MessageStorage m_storage;

	@Inject
	private MessagePathBuilder m_builder;

	private Logger m_logger;

	@Override
	public void consume(MessageTree tree) {
		m_storage.store(tree);
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
