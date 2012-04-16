package com.dianping.cat.hadoop.hdfs;

import java.io.IOException;
import java.io.OutputStream;

import com.dianping.cat.message.spi.MessageTree;

public interface OutputChannel {
	/**
	 * Close the channel.
	 */
	public void close();

	/**
	 * Initialize the channel with an output stream.
	 * 
	 * @param out
	 */
	public void initialize(long maxSize, OutputStream out);

	/**
	 * Check if the channel is expired.
	 * 
	 * @return true if the channel is expired, false otherwise.
	 */
	public boolean isExpired();

	/**
	 * Output the message tree to the HDFS.
	 * 
	 * @param tree
	 * @return false if the max size is reached, false otherwise.
	 * @throws IOException
	 */
	public int write(MessageTree tree) throws IOException;
	
	/**
	 * Get the size of data written to the output channel.
	 * 
	 * @return size of data written to the output channel
	 */
	public int getSize();

}
