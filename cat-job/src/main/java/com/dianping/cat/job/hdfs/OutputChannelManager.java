package com.dianping.cat.job.hdfs;

import java.io.IOException;

public interface OutputChannelManager {
	public void cleanupChannels();

	public void closeAllChannels();

	public void closeChannel(OutputChannel channel);

	public OutputChannel openChannel(String channelId, String path, boolean forceNew) throws IOException;
}
