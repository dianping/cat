package com.dianping.cat.message.spi;

/**
 * @author franke.wu
 * @author sean.wang
 * @since Mar 6, 2012
 */
public interface MessageStorage {
	/**
	 * Store a message tree to the storage.
	 * 
	 * @param tree
	 *            message tree to store
	 * @return relative path to base directory or base URL
	 */
	public String store(MessageTree tree);

	/**
<<<<<<< HEAD
	 * Fetch a message tree from the store.
	 * 
	 * @param messageId
	 * @return
	 */
	public MessageTree get(String messageId);

	/**
	 * Get relative path to base directory or base URL.
	 * 
	 * @param tree
	 * 
=======
>>>>>>> 31515b8c1b82963a28412ab90fbd3229ea890f3f
	 * Get relative path to base directory or base URL.
	 * 
	 * @param tree
	 * @return relative path to base directory or base URL
	 */
	public String getPath(MessageTree tree);
}
