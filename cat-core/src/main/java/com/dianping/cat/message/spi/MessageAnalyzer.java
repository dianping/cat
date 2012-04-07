package com.dianping.cat.message.spi;

public interface MessageAnalyzer {
	public void analyze(MessageQueue queue);

	public void doCheckpoint(boolean atEnd);
}
