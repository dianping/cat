package com.dianping.cat.message;

/**
 * <p>
 * Message represents data collected during application runtime. It will be sent
 * to back-end system asynchronous for further processing.
 * </p>
 * 
 * <p>
 * Super interface of <code>Event</code>, <code>Heartbeat</code> and
 * <code>Transaction</code>.
 * </p>
 * 
 * @see Event, Heartbeat, Transaction
 * @author Frankie Wu
 */
public interface Message {
	/**
	 * add one or multiple key-value pairs to the message.
	 * 
	 * @param keyValuePairs
	 *           key-value pairs like 'a=1&b=2&...'
	 */
	public void addData(String keyValuePairs);

	/**
	 * add one key-value pair to the message.
	 * 
	 * @param key
	 * @param value
	 */
	public void addData(String key, Object value);

	/**
	 * Complete the message construction.
	 */
	public void complete();

	/**
	 * Message name.
	 * 
	 * @return message name
	 */
	public String getName();

	/**
	 * Get the message status.
	 * 
	 * @return message status. "0" means success, otherwise error code.
	 */
	public String getStatus();

	/**
	 * The time stamp the message was created.
	 * 
	 * @return message creation time stamp in milliseconds
	 */
	public long getTimestamp();

	/**
	 * Message type.
	 * 
	 * @return message type
	 */
	public String getType();

	/**
	 * Set the message status.
	 * 
	 * @param status
	 *           message status. "0" means success, otherwise error code.
	 */
	public void setStatus(String status);
}
