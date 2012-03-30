package com.dianping.cat.job.hdfs;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;

import com.dianping.cat.server.configuration.entity.HdfsConfig;
import com.dianping.cat.server.configuration.entity.ServerConfig;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultOutputChannelManager extends ContainerHolder implements OutputChannelManager, LogEnabled {
	@Inject
	private FileSystemManager m_manager;

	private Map<String, OutputChannel> m_channels = new HashMap<String, OutputChannel>();

	private Map<String, Integer> m_indexes = new HashMap<String, Integer>();

	private Logger m_logger;

	@Override
	public void cleanupChannels() {
		try {
			List<String> expired = new ArrayList<String>();

			for (Map.Entry<String, OutputChannel> e : m_channels.entrySet()) {
				if (e.getValue().isExpired()) {
					expired.add(e.getKey());
				}
			}

			for (String path : expired) {
				OutputChannel channel = m_channels.remove(path);

				closeChannel(channel);
			}
		} catch (Exception e) {
			m_logger.warn("Error when doing cleanup!", e);
		}
	}

	@Override
	public void closeAllChannels() {
		for (OutputChannel channel : m_channels.values()) {
			closeChannel(channel);
		}
	}

	@Override
	public void closeChannel(OutputChannel channel) {
		channel.close();
		super.release(channel);
	}

	@Override
	public void enableLogging(Logger logger) {
		m_logger = logger;
	}

	public OutputChannel openChannel(String id, String path, boolean forceNew) throws IOException {
		String key = id + ":" + path;
		OutputChannel channel = m_channels.get(key);

		if (channel == null) {
			synchronized (m_channels) {
				channel = m_channels.get(key);

				if (channel == null) {
					channel = makeChannel(key, id, path, false);
				}
			}
		} else if (forceNew) {
			channel = makeChannel(key, id, path, true);
		}

		return channel;
	}

	private OutputChannel makeChannel(String key, String id, String path, boolean forceNew) throws IOException {
		OutputChannel channel = lookup(OutputChannel.class);
		StringBuilder baseDir = new StringBuilder(32);
		FileSystem fs = m_manager.getFileSystem(key, id, path, baseDir);
		Path file;

		if (forceNew) {
			Integer index = m_indexes.get(key);

			if (index == null) {
				index = 0;
			} else {
				index++;
			}

			file = new Path(baseDir.toString(), path + (index > 0 ? "-" + index : ""));
			m_indexes.put(key, index);
		} else {
			file = new Path(baseDir.toString(), path);
		}

		FSDataOutputStream out = fs.create(file);
		ServerConfig config = m_manager.getServerConfig();
		HdfsConfig hdfsConfig = config.getStorage().findHdfs(id);

		channel.initialize(hdfsConfig, out);

		m_channels.put(key, channel);
		return channel;
	}
}
