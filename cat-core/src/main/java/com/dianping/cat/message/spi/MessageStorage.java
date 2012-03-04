package com.dianping.cat.message.spi;

public interface MessageStorage {
	/**
	 * Store a message tree to the storage.
	 * 
	 * @param tree
	 *           message tree to store
	 * @return relative path to base directory or base URL
	 */
	public String store(MessageTree tree);

	/**
	 * Get relative path to base directory or base URL.
	 * 
	 * @param tree
	 * @return relative path to base directory or base URL
	 */
	public String getPath(MessageTree tree);
}
