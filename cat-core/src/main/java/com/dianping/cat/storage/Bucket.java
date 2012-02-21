package com.dianping.cat.storage;

import java.io.IOException;
import java.util.List;

public interface Bucket<T> {
	public void close();

	public List<T> findAllByIds(List<String> ids);

	public T findById(String id);

	public void initialize(Class<?> type, String path) throws IOException;

	public boolean storeById(String id, T data);

}
