package com.dianping.cat.storage;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

public interface Bucket<T> {
	/**
	 * Close bucket and release component instance
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException;

	/**
	 * Find data by given id in the bucket. return null if not found.
	 * 
	 * @param id
	 * @return data for given id, null if not found
	 * @throws IOException
	 */
	public T findById(String id) throws IOException;

	/**
	 * Find next id with same tag in the bucket. return null if not found.
	 * 
	 * @param id
	 * @param tag
	 * @return next data for given id with tag, null if not found.
	 * @throws IOException
	 */
	public T findNextById(String id, String tag) throws IOException;

	/**
	 * Find previous id with same tag in the bucket. return null if not found.
	 * 
	 * @param id
	 * @param tag
	 * @return previous data for given id with tag, null if not found.
	 * @throws IOException
	 */
	public T findPreviousById(String id, String tag) throws IOException;

	/**
	 * Flush the buffered data in the bucket if have.
	 * 
	 * @throws IOException
	 */
	public void flush() throws IOException;

	/**
	 * Return all ids in the bucket.
	 * 
	 * @return
	 */
	public Collection<String> getIds();

	/**
	 * Initialize the bucket after its creation.
	 * 
	 * @param type
	 * @param name
	 * @param timestamp
	 * @throws IOException
	 */
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
}
