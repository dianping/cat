package com.dianping.cat.analysis;

import java.util.List;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageConsumer {
	public void consume(MessageTree tree);

	public void doCheckpoint();

	public List<MessageAnalyzer> getCurrentAnalyzer(String name);

	public List<MessageAnalyzer> getLastAnalyzer(String name);
}
