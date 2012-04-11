package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;

import org.apache.hadoop.fs.FSDataInputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

import com.site.lookup.ContainerHolder;
import com.site.lookup.annotation.Inject;

public class DefaultInputChannelManager extends ContainerHolder implements InputChannelManager {
	@Inject
	private FileSystemManager m_manager;

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
	public InputChannel openChannel(String id, String path) throws IOException {
		StringBuilder baseDir = new StringBuilder(32);
		FileSystem fs = m_manager.getFileSystem(id, baseDir);
		Path file = new Path(baseDir.toString(), path);

		FSDataInputStream in = fs.open(file);
		DefaultInputChannel channel = (DefaultInputChannel) lookup(InputChannel.class);

		channel.setPath(path);
		channel.initialize(in);

		return channel;
	}
}
