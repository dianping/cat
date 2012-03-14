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
	public static final String ID = "dump-to-html";

	@Inject(value = "html")
	private MessageStorage m_storage;

	@Inject
	private MessagePathBuilder m_builder;

	@Inject
	private String m_domain;

	private Logger m_logger;

	@Override
	public void consume(MessageTree tree) {
		if (m_domain == null || m_domain.equals(tree.getDomain())) {
			m_storage.store(tree);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getConsumerId() {
		return ID;
	}

	@Override
	public void initialize() throws InitializationException {
		File baseDir = m_builder.getLogViewBaseDir();

		baseDir.mkdirs();
		m_logger.info(String.format("Message will be dumpped to %s.", baseDir));
	}

	public void setDomain(String domain) {
		m_domain = domain;
	}
}
