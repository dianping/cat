package org.unidal.cat.message.storage;

import java.util.concurrent.BlockingQueue;

import org.unidal.helper.Threads.Task;

import com.dianping.cat.message.spi.MessageTree;

public interface MessageProcessor extends Task {
	public void initialize(int hour, int index, BlockingQueue<MessageTree> queue);
}
