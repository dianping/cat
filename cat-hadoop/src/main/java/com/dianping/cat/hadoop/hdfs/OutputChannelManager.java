package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;

public interface OutputChannelManager {
	public void cleanupChannels();

	public void closeAllChannels();

	public void closeChannel(OutputChannel channel);

	public OutputChannel openChannel(String channelId, String path, boolean forceNew) throws IOException;
}
