package com.dianping.cat.storage;

import java.io.IOException;

public interface BucketManager {
	public MessageBucket getMessageBucket(String path) throws IOException;

	public <T> Bucket<T> getBucket(Class<T> type, String path) throws IOException;
}
