package com.dianping.cat.storage;

public interface BucketFactory<T> {
	public Bucket<T> create(String path);
}