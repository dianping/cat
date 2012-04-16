package com.dianping.cat.consumer.dump;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.configuration.ServerConfigManager;
import com.dianping.cat.message.spi.MessageCodec;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DumpChannelManager extends ContainerHolder implements Initializable, LogEnabled {
	@Inject
	private MessageCodec m_codec;

	private Map<String, DumpChannel> m_channels = new HashMap<String, DumpChannel>();

	private Map<String, Integer> m_indexes = new HashMap<String, Integer>();

	private long m_maxSize;

	private long m_lastChunkAdjust = 100 * 1024L; // 100K

	private String m_baseDir;

	private Logger m_logger;

	public void closeAllChannels() {
		for (DumpChannel channel : m_channels.values()) {
			closeChannel(channel);
		}

		m_channels.clear();
	}

	public void closeChannel(DumpChannel channel) {
		release(channel);
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
		m_maxSize = configManager.getHdfsFileMaxSize("dump");
	}

	private DumpChannel makeChannel(String key, String path, boolean forceNew) throws IOException {
		String file;

		if (forceNew) {
			Integer index = m_indexes.get(key);

			if (index == null) {
				index = 1;
			}

			index++;

			file = path + "-" + index + ".gz";
			m_indexes.put(key, index);
		} else {
			file = path + ".gz";
		}

		DumpChannel channel = new DumpChannel(m_codec, new File(m_baseDir, "draft"), file, m_maxSize, m_lastChunkAdjust);

		m_channels.put(key, channel);
		return channel;
	}

	public DumpChannel openChannel(String path, boolean forceNew) throws IOException {
		DumpChannel channel = m_channels.get(path);

		if (channel == null) {
			synchronized (m_channels) {
				channel = m_channels.get(path);

				if (channel == null) {
					channel = makeChannel(path, path, false);
				}
			}
		} else if (forceNew) {
			synchronized (m_channels) {
				channel = makeChannel(path, path, true);
			}
		}

		return channel;
	}
}
