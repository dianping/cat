package org.unidal.cat.message.storage;

import java.util.concurrent.BlockingQueue;

import org.unidal.helper.Threads.Task;

public interface BlockWriter extends Task {
	public void initialize(int hour, int index, BlockingQueue<Block> queue);
	
}
