package com.dianping.cat.message.spi;

import java.io.IOException;

public interface MessageAnalyzer {
	public void analyze(MessageQueue queue);

	public void doCheckpoint() throws IOException;
}
