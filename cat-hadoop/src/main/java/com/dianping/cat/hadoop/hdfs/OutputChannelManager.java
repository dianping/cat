package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;

import com.dianping.cat.message.spi.MessageTree;

public interface OutputChannelManager {
	public void cleanupChannels();

	public void closeAllChannels();

	public void closeChannel(OutputChannel channel);

	public OutputChannel openChannel(MessageTree tree, boolean forceNew) throws IOException;
}
