/**
 * 
 */
package com.dianping.cat.consumer.remote;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.NetworkInterfaceManager;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.annotation.Inject;

/**
 * @author sean.wang
 * @since Jun 21, 2012
 */
public class RemoteIdAnalyzer extends AbstractMessageAnalyzer<Object> implements Initializable, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private RemoteIdUploader m_uploader;

	@Inject
	private MessagePathBuilder m_builder;

	private Logger m_logger;

	private OutputStream m_output;

	private boolean m_localMode = true;

	private long m_extraTime;

	private long m_startTime;

	private long m_duration;

	private File m_file;

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.message.spi.MessageAnalyzer#doCheckpoint(boolean)
	 */
	@Override
	public void doCheckpoint(boolean atEnd) {
		if (m_output != null) {
			try {
				m_output.close();
				String m_baseDir = m_configManager.getHdfsLocalBaseDir("dump");
				String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
				String path = m_builder.getMessageRemoteIdPath(ipAddress, new Date());

				File outbox = new File(m_baseDir, "outbox");
				outbox.mkdirs();

				File target = new File(outbox, path);
				target.getParentFile().mkdirs();
				m_file.renameTo(target);

			} catch (IOException e) {
				m_logger.error("doCheckpoint", e);
			}
		}

	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.dianping.cat.message.spi.MessageAnalyzer#getDomains()
	 */
	@Override
	public Set<String> getDomains() {
		return Collections.emptySet();
	}

	@Override
	public Object getReport(String domain) {
		throw new UnsupportedOperationException("This should not be called!");
	}

	@Override
	public void initialize() throws InitializationException {
		m_localMode = m_configManager.isLocalMode();

		if (!m_localMode) {
			String m_baseDir = m_configManager.getHdfsLocalBaseDir("dump");
			String ipAddress = NetworkInterfaceManager.INSTANCE.getLocalHostAddress();
			String path = m_builder.getMessageRemoteIdPath(ipAddress, new Date());

			File draft = new File(m_baseDir, "draft");
			draft.mkdirs();

			m_file = new File(draft, path);
			try {
				if (!m_file.exists()) {
					m_file.getParentFile().mkdirs();
					m_file.createNewFile();
				}
				m_output = new FileOutputStream(m_file);
			} catch (IOException e) {
				m_logger.error("", e);
			}

			m_uploader.start();
		}
	}

	@Override
	protected boolean isTimeout() {
		long currentTime = System.currentTimeMillis();
		long endTime = m_startTime + m_duration + m_extraTime;

		return currentTime > endTime;
	}

	@Override
	protected void process(MessageTree tree) {
		if (m_localMode || tree.getMessage() == null) {
			return;
		}

		if (!(tree.getMessage() instanceof Transaction)) {
			return;
		}

		List<String> remoteIds = new ArrayList<String>();
		Transaction t = (Transaction) tree.getMessage();
		doTransactionChilds(remoteIds, t);

		if (remoteIds.size() == 0) {
			return;
		}

		StringBuilder sb = new StringBuilder((remoteIds.size() + 1) * remoteIds.get(0).length() + 32);
		sb.append(tree.getMessageId());
		sb.append('\t');
		sb.append(tree.getParentMessageId());
		sb.append('\t');
		sb.append(tree.getRootMessageId());
		sb.append('\t');
		for (String id : remoteIds) {
			sb.append(id);
			sb.append('\t');
		}
		if (t.isSuccess()) {
			sb.append('0');
		} else {
			sb.append('1');
		}
		sb.append('\n');

		try {
			m_output.write(sb.toString().getBytes());
		} catch (IOException e) {
			m_logger.error("write message id record", e);
		}

	}

	public static final String PIGEON_REQUEST_NAME = "PigeonRequest";

	public static final String PIGEON_RESPONSE_NAME = "PigeonRespone";

	public static final String PIGEON_REQUEST_TYPE = "RemoteCall";

	protected void doTransactionChilds(List<String> remoteIds, Transaction t) {
		if (!t.hasChildren()) {
			return;
		}
		for (Message m : t.getChildren()) {
			if (m instanceof Event && // is event
					PIGEON_REQUEST_TYPE.equals(m.getType()) && (PIGEON_REQUEST_NAME.equals(m.getName()) // is pigeon request
					|| PIGEON_RESPONSE_NAME.equals(m.getName()))) { // is pigeon response
				Event e = (Event) m;
				String requestMessageId = (String) e.getData();
				remoteIds.add(requestMessageId);
			} else if (m instanceof Transaction) {
				doTransactionChilds(remoteIds, (Transaction)m);
			}
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

}
