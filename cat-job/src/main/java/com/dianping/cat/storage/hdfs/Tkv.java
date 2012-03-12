/**
 * 
 */
package com.dianping.cat.storage.hdfs;

import java.io.IOException;

/**
 * Tagged key-value store interface.
 * 
 * @author sean.wang
 * @since Feb 21, 2012
 */
public interface Tkv {

	/**
	 * close store
	 * 
	 * @throws IOException
	 */
	void close() throws IOException;

	/**
	 * get value by index pos
	 * 
	 * @param indexPos
	 * @return
	 * @throws IOException
	 */
	byte[] get(int indexPos) throws IOException;

	/**
	 * get value by key
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	byte[] get(String key) throws IOException;

	/**
	 * get vlaue by key and tag
	 * 
	 * @return
	 * @throws IOException
	 */
	byte[] get(String key, String tag) throws IOException;

	/**
	 * get index by index pos
	 * 
	 * @param indexPos
	 * @return
	 * @throws IOException
	 */
	Meta getIndex(int indexPos) throws IOException;

	/**
	 * get index by key
	 * 
	 * @param key
	 * @return
	 * @throws IOException
	 */
	Meta getIndex(String key) throws IOException;

	/**
	 * get index by key and tag
	 * 
	 * @param key
	 * @param tag
	 * @return
	 * @throws IOException
	 */
	Meta getIndex(String key, String tag) throws IOException;

	/**
	 * @param key
	 * @param tag
	 * @return
	 * @throws IOException
	 */
	Record getRecord(String key, String tag) throws IOException;

	/**
	 * @param key
	 * @param value
	 * @throws IOException
	 */
	boolean put(String key, byte[] value) throws IOException;

	/**
	 * @param key
	 * @param value
	 * @param tags
	 * @throws IOException
	 */
	boolean put(String key, byte[] value, String... tags) throws IOException;

	/**
	 * record size
	 * 
	 * @return
	 * @throws IOException
	 */
	long size() throws IOException;

	boolean delete() throws IOException;
}
