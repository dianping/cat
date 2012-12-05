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
	 *           message tree to store
	 * @return relative path to base directory or base URL
	 */
	public String store(MessageTree tree, String... tag);

	/**
	 * Fetch a message tree from the store.
	 * 
	 * @param messageId
	 * @return
	 */
	public MessageTree get(String messageId);

	/**
	 * @param messageId
	 * @return
	 */
	public MessageTree next(String messageId, String tag);

	/**
	 * @param messageId
	 * @return
	 */
	public MessageTree previous(String messageId, String tag);

	/**
	 * 
	 * Get relative path to base directory or base URL.
	 * 
	 * @param tree
	 * @return relative path to base directory or base URL
	 */
	public String getPath(MessageTree tree);
}
