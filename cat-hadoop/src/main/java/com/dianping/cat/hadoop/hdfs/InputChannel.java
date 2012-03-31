package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;

import com.dianping.cat.message.spi.MessageTree;

public interface InputChannel {
	/**
	 * Close the channel.
	 */
	public void close();

	/**
	 * Check if the channel is expired.
	 * 
	 * @return true if the channel is expired, false otherwise.
	 */
	public boolean isExpired();

	/**
	 * Fetch message tree from hdfs.
	 * 
	 * @param index
	 * @param length
	 * @return
	 * @throws IOException
	 */
	public MessageTree read(long offset, int length) throws IOException;

	/**
	 * @return
	 */
	String getPath();
}
