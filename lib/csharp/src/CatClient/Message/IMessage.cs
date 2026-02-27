using System;

namespace Org.Unidal.Cat.Message
{
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

    public interface IMessage
    {

        /**
		 * @return key value pairs data
		 */
        string Data { get; }

        /**
		 * Message name.
		 * 
		 * @return message name
		 */
        string Name { get; set; }

        /**
		 * Get the message status.
		 * 
		 * @return message status. "0" means success, otherwise error code.
		 */
        string Status { get; set; }

        /**
		 * The time stamp the message was created.
		 * 
		 * @return message creation time stamp in milliseconds
		 */
        long Timestamp { get; set; }

        /**
		 * Message type.
		 * 
		 * <p>
		 * Typical message types are:
		 * <ul>
		 * <li>URL: maps to one method of an action</li>
		 * <li>Service: maps to one method of service call</li>
		 * <li>Search: maps to one method of search call</li>
		 * <li>SQL: maps to one SQL statement</li>
		 * <li>Cache: maps to one cache access</li>
		 * <li>Error: maps to java.lang.Throwable (java.lang.Exception and java.lang.Error)</li>
		 * </ul>
		 * </p>
		 * 
		 * @return message type
		 */
        string Type { get; set; }

        /**
		 * add one or multiple key-value pairs to the message.
		 * 
		 * @param keyValuePairs
		 *           key-value pairs like 'a=1&b=2&...'
		 */
        void AddData(String keyValuePairs);

        /**
		 * add one key-value pair to the message.
		 * 
		 * @param key
		 * @param value
		 */
        void AddData(String key, Object value);

        /**
		 * Complete the message construction.
		 */
        void Complete();

        /**
		 * If the complete() method was called or not.
		 * 
		 * @return true means the complete() method was called, false otherwise.
		 */
        bool IsCompleted();

        /**
		 * @return
		 */
        bool IsSuccess();

        /**
		 * Set the message status with exception class name.
		 * 
		 * @param e
		 *           exception.
		 */
        void SetStatus(Exception e);

        int EstimateByteSize();
    }
}