package com.dianping.cat.hadoop.hdfs;

import java.util.List;

import com.dianping.cat.message.spi.MessageTree;
import com.dianping.cat.storage.Bucket;

/**
 * Map to one HDFS directory for one report
 */
public interface MessageBucket extends Bucket<MessageTree> {
	public void close();

	public boolean storeById(String id, MessageTree value, String... tags);

	public List<String> findAllIdsByTag(String tag);

	public MessageTree findNextById(String id, Direction direction, String tag); // tag:
																										  // "thread:101",
																										  // "session:abc",
																										  // "request:xyz",
																										  // "parent:xxx"

	public static enum Direction {
		FORWARD,

		BACKWARD;
	}
}
