package com.dianping.cat.job.hdfs;

import java.io.IOException;

public interface InputChannelManager {
	public void cleanupChannels();

	public void closeAllChannels();

	public void closeChannel(InputChannel channel);

	public InputChannel openChannel(String messageId) throws IOException;
}
