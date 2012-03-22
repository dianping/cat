package com.dianping.cat.job.hdfs;

import java.io.IOException;
import java.net.URI;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.Initializable;
import org.codehaus.plexus.personality.plexus.lifecycle.phase.InitializationException;

import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultInputChannelManager extends ContainerHolder implements InputChannelManager, Initializable {
	@Inject
	private URI m_serverUri;

	@Inject
	private String m_baseDir = "target/hdfs";

	private FileSystem m_fs;

	private Path m_basePath;

	@Override
	public void cleanupChannels() {
	}

	@Override
	public void closeAllChannels() {
	}

	@Override
	public void closeChannel(InputChannel channel) {
		channel.close();
		super.release(channel);
	}

	@Override
	public void initialize() throws InitializationException {
		try {
			Configuration config = new Configuration();
			FileSystem fs;

			config.setInt("io.file.buffer.size", 8192);

			if (m_serverUri == null) {
				fs = FileSystem.getLocal(config);
				m_basePath = new Path(fs.getWorkingDirectory(), m_baseDir);
			} else {
				fs = FileSystem.get(m_serverUri, config);
				m_basePath = new Path(new Path(m_serverUri), m_baseDir);
			}

			m_fs = fs;
		} catch (Exception e) {
			throw new InitializationException("Error when getting HDFS file system.", e);
		}
	}

	@Override
	public InputChannel openChannel(String path) throws IOException {
		Path file = new Path(m_basePath, path);
		FSDataInputStream in = m_fs.open(file);
		DefaultInputChannel channel = (DefaultInputChannel) lookup(InputChannel.class);
		channel.setPath(path);
		channel.initialize(in);
		return channel;
	}

	public void setBaseDir(String baseDir) {
		m_baseDir = baseDir;
	}

	public void setServerUri(String serverUri) {
		m_serverUri = URI.create(serverUri);
	}
}
