package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;

import com.dianping.cat.message.spi.MessageTree;

public interface ChannelManager {
	public OutputChannel findChannel(MessageTree tree, boolean forceNew) throws IOException;

	public void cleanupChannels();

	public void closeAllChannels();

	public void closeChannel(OutputChannel channel);
}
