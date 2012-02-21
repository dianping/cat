package com.dianping.cat.storage;

import java.util.List;

import com.dianping.cat.message.spi.MessageTree;

/**
 * Map to one HDFS directory for one report.
 * <p>
 * 
 * Sample tags: "thread:101", "session:abc", "request:xyz", "parent:xxx"
 */
public interface MessageBucket extends Bucket<MessageTree> {
	public boolean storeById(String id, MessageTree value, String... tags);

	public List<String> findAllIdsByTag(String tag);

	public MessageTree findNextById(String id, Direction direction, String tag);

	public static enum Direction {
		FORWARD,

		BACKWARD;
	}
}
