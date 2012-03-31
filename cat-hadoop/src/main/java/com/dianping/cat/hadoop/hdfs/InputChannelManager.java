package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;

public interface InputChannelManager {
	public void cleanupChannels();

	public void closeAllChannels();

	public void closeChannel(InputChannel channel);

	public InputChannel openChannel(String id, String path) throws IOException;
}
