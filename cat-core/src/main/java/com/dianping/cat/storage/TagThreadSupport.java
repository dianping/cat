package com.dianping.cat.storage;

import java.util.List;

/**
 * Map to one HDFS directory for one report.
 * <p>
 * 
 * Sample tags: "thread:101", "session:abc", "request:xyz"
 */
public interface TagThreadSupport<T> {
	public boolean storeById(String id, T data, String... tags);

	public List<String> findAllIdsByTag(String tag);

	public T findNextById(String id, Direction direction, String tag);

	public static enum Direction {
		FORWARD,

		BACKWARD;
	}
}
