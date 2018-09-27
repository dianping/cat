package org.unidal.cat.message.storage;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageDumper {
	public void awaitTermination(int hour) throws InterruptedException;

	public void initialize(int hour);

	public void process(MessageTree tree);
}
