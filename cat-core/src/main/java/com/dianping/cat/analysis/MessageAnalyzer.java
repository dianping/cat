package com.dianping.cat.analysis;

import com.dianping.cat.message.spi.MessageQueue;

public interface MessageAnalyzer {
	public boolean isRawAnalyzer();
	
	public void analyze(MessageQueue queue);

	public void destroy();

	public void doCheckpoint(boolean atEnd);
	
	public long getStartTime();

	public void initialize(long startTime, long duration, long extraTime);
}
