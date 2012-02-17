package com.dianping.cat.storage;

public interface Bucket<T> {
	public T findById(String id);
}
