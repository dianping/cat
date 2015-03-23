package com.dianping.cat.analysis;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageConsumer {
	public void consume(MessageTree tree);
	
	public void doCheckpoint();
}
