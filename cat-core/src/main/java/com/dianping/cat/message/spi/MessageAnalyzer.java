package com.dianping.cat.message.spi;

import java.util.Set;

public interface MessageAnalyzer {
	public void analyze(MessageQueue queue);

	public void doCheckpoint(boolean atEnd);

	public Set<String> getDomains();
}
