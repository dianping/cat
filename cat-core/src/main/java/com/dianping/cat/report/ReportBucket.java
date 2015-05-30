package com.dianping.cat.report;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

public interface ReportBucket {
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
	public String findById(String id) throws IOException;

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
	public void initialize(String name, Date timestamp, int index) throws IOException;

	/**
	 * store the data by id into the bucket.
	 * 
	 * @param id
	 * @param data
	 * @return true means the data was stored in the bucket, otherwise false.
	 * @throws IOException
	 */
	public boolean storeById(String id, String data) throws IOException;

}
