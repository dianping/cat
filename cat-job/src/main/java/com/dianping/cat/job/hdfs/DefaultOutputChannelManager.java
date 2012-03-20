package com.dianping.cat.job.hdfs;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.plexus.logging.LogEnabled;
import org.codehaus.plexus.logging.Logger;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.dianping.cat.message.spi.MessagePathBuilder;
import com.dianping.cat.message.spi.MessageTree;
import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultOutputChannelManager extends ContainerHolder implements OutputChannelManager, Initializable,
      LogEnabled {
	@Inject
	private MessagePathBuilder m_builder;

	@Inject
	private String m_baseDir = "target/hdfs";

	@Inject
	private URI m_serverUri;

	private FileSystem m_fs;

	private Path m_basePath;

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

	@Override
	public void initialize() throws InitializationException {
		try {
			Configuration config = new Configuration();
			FileSystem fs;

			config.setInt("io.file.buffer.size", 8192);

			if (m_serverUri == null) {
				fs = FileSystem.getLocal(config);
			} else {
				fs = FileSystem.get(m_serverUri, config); // TODO Not tested yet
			}

			m_fs = fs;
			m_basePath = new Path(m_fs.getWorkingDirectory(), m_baseDir);
		} catch (Exception e) {
			throw new InitializationException("Error when getting HDFS file system.", e);
		}
	}

	@Override
	public OutputChannel openChannel(MessageTree tree, boolean forceNew) throws IOException {
		String path = m_builder.getHdfsPath(tree.getMessageId());

		return openChannel(path, forceNew);
	}

	public OutputChannel openChannel(String path, boolean forceNew) throws IOException {
		OutputChannel channel = m_channels.get(path);

		if (channel == null) {
			Path file = new Path(m_basePath, path);
			FSDataOutputStream out = m_fs.create(file);

			channel = lookup(OutputChannel.class);
			channel.initialize(out);

			m_indexes.put(path, 0);
			m_channels.put(path, channel);
		} else if (forceNew) {
			int index = m_indexes.get(path);

			closeChannel(channel);
			m_indexes.put(path, ++index);

			Path file = new Path(m_basePath, path + "-" + index);
			FSDataOutputStream out = m_fs.create(file);

			channel = lookup(OutputChannel.class);
			channel.initialize(out);
			m_channels.put(path, channel);
		}

		return channel;
	}

	public void setBaseDir(String baseDir) {
		m_baseDir = baseDir;
	}

	public void setServerUri(String serverUri) {
		m_serverUri = URI.create(serverUri);
	}
}
