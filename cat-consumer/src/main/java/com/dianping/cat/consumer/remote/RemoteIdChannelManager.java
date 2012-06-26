package com.dianping.cat.consumer.remote;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.site.lookup.ContainerHolder;

/**
 * @author sean.wang
 * @since Jun 26, 2012
 */
public class RemoteIdChannelManager extends ContainerHolder implements Initializable, LogEnabled {

	private Map<String, RemoteIdChannel> m_channels = new HashMap<String, RemoteIdChannel>();

	private String m_baseDir;

	private Logger m_logger;

	public void closeAllChannels() {
		synchronized (m_channels) {
			for (RemoteIdChannel channel : m_channels.values()) {
				closeChannel(channel);
			}
			m_channels.clear();
		}
	}

	public void closeAllChannels(long startTime) {
		Set<String> closedKeySet = new HashSet<String>();

		synchronized (m_channels) {
			for (Entry<String, RemoteIdChannel> entry : m_channels.entrySet()) {
				String key = entry.getKey();
				RemoteIdChannel channel = entry.getValue();

				if (channel.getStartTime() <= startTime) {
					closedKeySet.add(key); // add closed channel key
					closeChannel(channel);
				} else {
					m_logger.info(String.format("still open RemoteIdChannel:%s in %s", channel.getFile().getAbsolutePath(), startTime));
				}
			}

			for (String key : closedKeySet) { // remove closed channel
				RemoteIdChannel channel = m_channels.remove(key);

				m_logger.info(String.format("close&remove RemoteIdChannel:%s in %s", channel.getFile().getAbsolutePath(), startTime));
			}
		}
	}

	public void closeChannel(RemoteIdChannel channel) {
		channel.close();

		File outbox = new File(m_baseDir, "outbox");

		outbox.mkdirs();

		try {
			channel.moveTo(outbox);
		} catch (IOException e) {
			m_logger.error(String.format("Error when moving file(%s) to directory(%s)!", channel.getFile(), outbox), e);
		}
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	@Override
	public void initialize() throws InitializationException {
		ServerConfigManager configManager = lookup(ServerConfigManager.class);

		m_baseDir = configManager.getHdfsLocalBaseDir("dump");
	}

	private RemoteIdChannel makeChannel(String key, String path, long startTime) throws IOException {
		RemoteIdChannel channel = new RemoteIdChannel(new File(m_baseDir, "draft"), path, startTime);

		m_logger.info(String.format("new RemoteIdChannel %s", path));

		m_channels.put(key, channel);
		return channel;
	}

	public RemoteIdChannel openChannel(String path, long startTime) throws IOException {
		RemoteIdChannel channel = m_channels.get(path);

		if (channel == null) {
			synchronized (m_channels) {
				channel = m_channels.get(path);

				if (channel == null) {
					channel = makeChannel(path, path, startTime);
				}
			}
		}

		return channel;
	}
}
