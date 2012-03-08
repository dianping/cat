package com.dianping.cat.storage;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface Bucket<T> extends TagThreadSupport<T> {
	public void close();

	public void deleteAndCreate();
	
	public List<T> findAllByIds(List<String> ids);

	public T findById(String id);

	public void initialize(Class<?> type, File path) throws IOException;

	public boolean storeById(String id, T data);

}
