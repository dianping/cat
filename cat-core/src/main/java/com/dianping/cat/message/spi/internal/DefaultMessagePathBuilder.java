package com.dianping.cat.message.spi.internal;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Date;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.configuration.ClientConfigManager;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.MessagePathBuilder;

public class DefaultMessagePathBuilder implements MessagePathBuilder, Initializable, LogEnabled {
	@Inject
	private ClientConfigManager m_configManager;

	private File m_baseLogDir;

	private Logger m_logger;

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public String getHdfsPath(String messageId) {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{0,date,HH}/{1}/{0,date,mm}-{2}");

		try {
			MessageId id = MessageId.parse(messageId);
			Date date = new Date(id.getTimestamp());
			String path = format.format(new Object[] { date, id.getDomain(), id.getIpAddressInHex() });

			return path;
		} catch (Exception e) {
			m_logger.error("Error when building HDFS path for " + messageId, e);
		}

		return messageId;
	}

	@Override
	public File getLogViewBaseDir() {
		return m_baseLogDir;
	}

	@Override
	public String getPath(Date timestamp, String name) {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{0,date,HH}/{1}");

		return format.format(new Object[] { timestamp, name });
	}

	@Override
	public String getReportPath(String name, Date timestamp) {
		MessageFormat format = new MessageFormat("{0,date,yyyyMMdd}/{0,date,HH}/report-{1}");

		return format.format(new Object[] { timestamp, name });
	}

	@Override
	public void initialize() throws InitializationException {
		String baseLogDir = m_configManager.getBaseLogDir();

		try {
			m_baseLogDir = new File(baseLogDir).getCanonicalFile();
		} catch (IOException e) {
			throw new InitializationException(String.format("Unable to create log directory(%s)!", m_baseLogDir), e);
		}
	}

	public void setBaseLogDir(File baseLogDir) {
		m_baseLogDir = baseLogDir;
	}
}
