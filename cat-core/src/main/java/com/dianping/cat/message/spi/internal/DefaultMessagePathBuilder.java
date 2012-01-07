package com.dianping.cat.message.spi.internal;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.Date;

import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessageManager;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

public class DefaultMessagePathBuilder implements MessagePathBuilder, Initializable {
	@Inject
	private MessageManager m_manager;

	private File m_baseLogDir;

	private URL m_baseLogUrl;

	@Override
	public File getLogViewFile(MessageTree tree) {
		String relativePath = getRelativePath(tree);
		File file = new File(m_baseLogDir, relativePath);

		return file;
	}

	@Override
	public URL getLogViewUrl(MessageTree tree) {
		String relativePath = getRelativePath(tree);

		try {
			URL url = new URL(m_baseLogUrl, relativePath);

			return url;
		} catch (MalformedURLException e) {
			throw new RuntimeException(String.format("Unable to construct message URL(%s, %s)!", m_baseLogUrl,
			      relativePath), e);
		}
	}

	private String getRelativePath(MessageTree tree) {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{1}/{2}");
		Date date = new Date(tree.getMessage().getTimestamp());
		String path = format.format(new Object[] { date, tree.getDomain(), tree.getMessageId() });

		return path;
	}

	@Override
	public void initialize() throws InitializationException {
		String baseLogDir = m_manager.getConfig().getBaseLogDir();
		String baseLogUrl = m_manager.getConfig().getBaseLogUrl();

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
}
