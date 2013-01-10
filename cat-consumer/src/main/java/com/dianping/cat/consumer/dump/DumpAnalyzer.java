package com.dianping.cat.consumer.dump;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;
import org.unidal.lookup.annotation.Inject;

import com.dianping.cat.Cat;
import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.MessageId;
import com.dianping.cat.message.spi.AbstractMessageAnalyzer;
import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.status.ServerStateManager;
import com.dianping.cat.storage.dump.LocalMessageBucketManager;
import com.dianping.cat.storage.dump.MessageBucketManager;

public class DumpAnalyzer extends AbstractMessageAnalyzer<Object> implements Initializable, LogEnabled {
	@Inject
	private ServerConfigManager m_configManager;

	@Inject
	private DumpUploader m_uploader;

	@Inject(type = MessageBucketManager.class, value = LocalMessageBucketManager.ID)
	private LocalMessageBucketManager m_bucketManager;

	@Inject
	private ServerStateManager m_serverStateManager;

	private Map<String, Integer> m_oldVersionDomains = new HashMap<String, Integer>();

	private boolean m_localMode = true;

	private static final long HOUR = 60 * 60 * 1000L;

	private SimpleDateFormat m_sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	private Logger m_logger;

	@Override
	public void doCheckpoint(boolean atEnd) {
		Transaction t = Cat.getProducer().newTransaction("Checkpoint", getClass().getSimpleName());
		t.setStatus(Message.SUCCESS);

		try {
			m_bucketManager.archive(m_startTime);
			// wait the block dump complete
			Thread.sleep(10 * 10000);
		} catch (Exception e) {
			t.setStatus(e);
			Cat.logError(e);
		} finally {
			t.complete();
		}
		m_logger.info("old version domains:" + m_oldVersionDomains);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public Set<String> getDomains() {
		return Collections.emptySet();
	}

	public DumpUploader getDumpUploader() {
		return m_uploader;
	}

	@Override
	public Object getReport(String domain) {
		throw new UnsupportedOperationException("This should not be called!");
	}

	@Override
	public void initialize() throws InitializationException {
		m_localMode = m_configManager.isLocalMode();

		if (!m_localMode) {
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
		if (tree.getMessage() == null) {
			return;
		}

		MessageId id = MessageId.parse(tree.getMessageId());

		if (id.getVersion() == 2) {
			try {
				long time = tree.getMessage().getTimestamp();
				long fixedTime = time - time % (60 * 60 * 1000);
				long idTime = id.getTimestamp();
				long duration = fixedTime - idTime;

				if (duration == 0 || duration == HOUR || duration == -HOUR) {
					m_bucketManager.storeMessage(tree, id);
				} else {
					m_serverStateManager.addPigeonTimeError(1);
					m_logger.error("timestamp:" + tree.getMessageId() + ",id timestamp " + m_sdf.format(new Date(idTime))
					      + " " + id.getIpAddress() + " ,tree timestamp:" + m_sdf.format(new Date(time)) + " "
					      + tree.getIpAddress() + " duration:" + duration);
				}
			} catch (IOException e) {
				m_logger.error("Error when dumping to local file system, version 2!", e);
			}
		} else {
			String domain = tree.getDomain();
			Integer size = m_oldVersionDomains.get(domain);

			if (size == null) {
				size = 1;
			} else {
				size++;
			}
			m_oldVersionDomains.put(domain, size);
		}
	}

	public void setAnalyzerInfo(long startTime, long duration, long extraTime) {
		m_extraTime = extraTime;
		m_startTime = startTime;
		m_duration = duration;
	}

}
