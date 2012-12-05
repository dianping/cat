package com.dianping.cat.message.spi;

import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;

/**
 * Message manager to help build CAT message.
 * <p>
 * 
 * Notes: This method is reserved for internal usage only. Application developer should never call this method directly.
 */
public interface MessageManager {
	public void add(Message message);

	/**
	 * Be triggered when a transaction ends, whatever it's the root transaction or nested transaction. However, if it's
	 * the root transaction then it will be flushed to back-end CAT server asynchronously.
	 * <p>
	 * 
	 * @param transaction
	 */
	public void end(Transaction transaction);

	/**
	 * Get peek transaction for current thread.
	 * 
	 * @return peek transaction for current thread, null if no transaction there.
	 */
	public Transaction getPeekTransaction();

	/**
	 * Get thread local message information.
	 * 
	 * @return message tree, null means current thread is not setup correctly.
	 */
	public MessageTree getThreadLocalMessageTree();

	/**
	 * Check if the thread context is setup or not.
	 * 
	 * @return true if the thread context is setup, false otherwise
	 */
	public boolean hasContext();

	/**
	 * Check if CAT logging is enabled or disabled.
	 * 
	 * @return true if CAT is enabled
	 */
	public boolean isCatEnabled();

	/**
	 * Do cleanup for current thread environment in order to release resources in thread local objects.
	 */
	public void reset();

	/**
	 * Do setup for current thread environment in order to prepare thread local objects.
	 */
	public void setup();

	/**
	 * Be triggered when a new transaction starts, whatever it's the root transaction or nested transaction.
	 * 
	 * @param transaction
	 */
	public void start(Transaction transaction);
}