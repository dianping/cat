package com.dianping.cat.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public interface Bucket<T> {
	public void close() throws IOException;

	public void deleteAndCreate() throws IOException;

	public T findById(String id) throws IOException;

	public T findNextById(String id, String tag) throws IOException;

	public T findPreviousById(String id, String tag) throws IOException;

	public void flush() throws IOException;

	public Collection<String> getIdsByPrefix(String prefix);

	public void initialize(Class<?> type, String name, Date timestamp) throws IOException;

	/**
	 * store the data by id into the bucket.
	 * 
	 * @param id
	 * @param data
	 * @return true means the data was stored in the bucket, otherwise false.
	 * @throws IOException
	 */
	public boolean storeById(String id, T data) throws IOException;

	List<String> findAllById(String id) throws IOException;
}
