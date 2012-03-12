package com.dianping.cat.storage;

import java.io.IOException;
import java.util.List;

/**
 * Map to one HDFS directory for one report.
 * <p>
 * 
 * Sample tags: "thread:101", "session:abc", "request:xyz"
 */
public interface TagThreadSupport<T> {
	public boolean storeById(String id, T data, String... tags) throws IOException;;

	public List<String> findAllIdsByTag(String tag) throws IOException;;

	public T findNextById(String id, Direction direction, String tag) throws IOException;;

	public static enum Direction {
		FORWARD,

		BACKWARD;
	}
}
