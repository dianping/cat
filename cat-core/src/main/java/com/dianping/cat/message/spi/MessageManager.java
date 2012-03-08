package com.dianping.cat.message.spi;

import com.dianping.cat.configuration.model.entity.Config;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

/**
 * Message manager to help build CAT message.
 * <p>
 * 
 * Notes: This method is reserved for internal usage only. Application developer
 * should never call this method directly.
 */
public interface MessageManager {
	public void add(Message message);

	/**
	 * Create a new message id.
	 * @return message id
	 */
	public String createMessageId();

	/**
	 * Be triggered when a transaction ends, whatever it's the root transaction
	 * or nested transaction. However, if it's the root transaction then it will
	 * be flushed to back-end CAT server asynchronously.
	 * <p>
	 * 
	 * @param transaction
	 */
	public void end(Transaction transaction);

	/**
	 * Return configuration for CAT client.
	 * 
	 * @return CAT configuration
	 */
	public Config getClientConfig();

	/**
	 * Return configuration for CAT client.
	 * 
	 * @return CAT configuration
	 */
	public Config getServerConfig();

	/**
	 * Get thread local message information.
	 * 
	 * @return message tree, null means current thread is not setup correctly.
	 */
	public MessageTree getThreadLocalMessageTree();

	/**
	 * Initialize CAT client with given CAT configuration.
	 * 
	 * @param config
	 *           CAT configuration
	 */
	public void initializeClient(Config config);

	/**
	 * Initialize CAT server with given CAT configuration.
	 * 
	 * @param config
	 *           CAT configuration
	 */
	public void initializeServer(Config config);

	/**
	 * Do cleanup for current thread environment in order to release resources in
	 * thread local objects.
	 */
	public void reset();

	/**
	 * Do setup for current thread environment in order to prepare thread local
	 * objects.
	 */
	public void setup();

	/**
	 * Be triggered when a new transaction starts, whatever it's the root
	 * transaction or nested transaction.
	 * 
	 * @param transaction
	 */
	public void start(Transaction transaction);
}