package com.dianping.cat.storage;

public interface BucketManager {
	public <T> Bucket<T> getBucket(BucketFactory<T> factory, String path);
}
