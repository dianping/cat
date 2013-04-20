package com.dianping.cat.consumer;

import java.util.Set;

import com.dianping.cat.message.spi.MessageQueue;

public interface MessageAnalyzer {
	public void analyze(MessageQueue queue);

	public void destroy();

	public void doCheckpoint(boolean atEnd);

	public Set<String> getDomains();

	public long getStartTime();

	public void initialize(long startTime, long duration, long extraTime);
}
