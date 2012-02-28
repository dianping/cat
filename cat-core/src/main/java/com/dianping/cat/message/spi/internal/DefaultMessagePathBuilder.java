package com.dianping.cat.message.spi.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;
import java.util.List;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.site.helper.Splitters;
import com.site.lookup.annotation.Inject;

public class DefaultMessagePathBuilder implements MessagePathBuilder, Initializable, LogEnabled {
	@Inject
	private MessageManager m_manager;

	private File m_baseLogDir;

	private URL m_baseLogUrl;

	private Logger m_logger;

	@Override
	public String getHdfsPath(MessageTree tree, String host) {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{0,date,HH}/{1}/{0,date,mm}-{2}");
		Date date = new Date(tree.getMessage().getTimestamp());
		String path = format.format(new Object[] { date, tree.getDomain(), host });

		return path;
	}

	@Override
	public File getLogViewBaseDir() {
		return m_baseLogDir;
	}

	@Override
	public URL getLogViewBaseUrl() {
		return m_baseLogUrl;
	}

	@Override
	public String getLogViewPath(MessageTree tree) {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{0,date,HH}/{1}/{2}.html");
		Date date = new Date(tree.getMessage().getTimestamp());
		String path = format.format(new Object[] { date, tree.getDomain(), tree.getMessageId() });

		return path;
	}

	@Override
	public String getLogViewPath(String messageId) {
		List<String> parts = Splitters.by('-').split(messageId);

		if (parts.size() == 4) {
			try {
				String domain = parts.get(0);
				long timestamp = Long.parseLong(parts.get(2), 16);
				MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{0,date,HH}/{1}/{2}.html");
				String path = format.format(new Object[] { new Date(timestamp), domain, messageId });

				return path;
			} catch (Exception e) {
				m_logger.error("Invalid message id format: " + messageId, e);
			}
		}

		return messageId;
	}

	@Override
	public void initialize() throws InitializationException {
		Config config = m_manager.getClientConfig();

		if (config == null) {
			config = new Config();
		}

		String baseLogDir = config.getBaseLogDir();
		String baseLogUrl = config.getBaseLogUrl();

		try {
			m_baseLogDir = new File(baseLogDir).getCanonicalFile();
			m_baseLogDir.mkdirs();
		} catch (IOException e) {
			throw new InitializationException(String.format("Unable to create log directory(%s)!", m_baseLogDir), e);
		}

		try {
			if (baseLogUrl == null) {
				m_baseLogUrl = m_baseLogDir.toURI().toURL();
			} else {
				m_baseLogUrl = new URL(baseLogUrl);
			}
		} catch (MalformedURLException e) {
			throw new InitializationException("Unable to build base log URL!", e);
		}
	}

	public void setBaseLogDir(File baseLogDir) {
		m_baseLogDir = baseLogDir;
	}

	public void setBaseLogUrl(URL baseLogUrl) {
		m_baseLogUrl = baseLogUrl;
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}
}
